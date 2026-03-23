package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.register.RegisterSessionDetailResponse;
import com.vendalume.vendalume.api.dto.register.RegisterSessionResponse;
import com.vendalume.vendalume.api.dto.register.StartSessionRequest;
import com.vendalume.vendalume.api.dto.sale.SaleResponse;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Register;
import com.vendalume.vendalume.domain.entity.RegisterSession;
import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.repository.RegisterOperatorRepository;
import com.vendalume.vendalume.repository.RegisterRepository;
import com.vendalume.vendalume.repository.RegisterSessionRepository;
import com.vendalume.vendalume.repository.SaleRepository;
import com.vendalume.vendalume.repository.UserRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de auditoria de sessões do PDV (abertura/fechamento e histórico).
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-03-05
 */
@Service
@RequiredArgsConstructor
public class RegisterSessionService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Sao_Paulo");

    private final RegisterSessionRepository registerSessionRepository;
    private final RegisterRepository registerRepository;
    private final RegisterOperatorRepository registerOperatorRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;
    private final SaleService saleService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterSessionResponse startSession(UUID registerId, StartSessionRequest request, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        UUID userId = SecurityUtils.getCurrentUserId();
        Register register = registerRepository.findByIdAndTenantId(registerId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de venda", registerId));
        if (!SecurityUtils.isCurrentUserRoot() && !registerOperatorRepository.existsByRegisterIdAndUserId(registerId, userId)) {
            throw new IllegalArgumentException("Usuário não é operador deste caixa.");
        }
        if (register.getAccessPasswordHash() == null || register.getAccessPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Este caixa não possui senha configurada. Configure a senha em Pontos de Venda.");
        }
        String pdvPassword = request != null ? request.getPdvPassword() : null;
        if (pdvPassword == null || pdvPassword.isBlank() || !passwordEncoder.matches(pdvPassword, register.getAccessPasswordHash())) {
            throw new IllegalArgumentException("Senha do PDV incorreta ou não informada.");
        }
        registerSessionRepository.findOpenByRegisterIdAndUserId(registerId, userId).ifPresent(open -> {
            open.setClosedAt(Instant.now());
            registerSessionRepository.save(open);
        });
        RegisterSession session = RegisterSession.builder()
                .id(UUID.randomUUID())
                .registerId(registerId)
                .userId(userId)
                .tenantId(tenantId)
                .openedAt(Instant.now())
                .closedAt(null)
                .build();
        session = registerSessionRepository.save(session);
        return toResponse(session, register.getName(), null, 0L, BigDecimal.ZERO);
    }

    @Transactional
    public RegisterSessionResponse endSession(UUID registerId, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        UUID userId = SecurityUtils.getCurrentUserId();
        RegisterSession session = registerSessionRepository.findOpenByRegisterIdAndUserId(registerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão aberta", registerId));
        session.setClosedAt(Instant.now());
        session = registerSessionRepository.save(session);
        Register register = registerRepository.findByIdAndTenantId(registerId, tenantId).orElse(null);
        String registerName = register != null ? register.getName() : null;
        List<Sale> sales = findSalesInSession(session);
        long count = sales.size();
        BigDecimal total = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return toResponse(session, registerName, null, count, total);
    }

    @Transactional(readOnly = true)
    public List<RegisterSessionResponse> listSessionsByRegister(UUID registerId, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        Register register = registerRepository.findByIdAndTenantId(registerId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de venda", registerId));
        List<RegisterSession> sessions = registerSessionRepository.findByRegisterIdAndTenantIdOrderByOpenedAtDesc(registerId, tenantId);
        return sessions.stream().map(s -> {
            List<Sale> sales = findSalesInSession(s);
            long count = sales.size();
            BigDecimal total = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            return toResponse(s, register.getName(), resolveUser(s.getUserId()), count, total);
        }).toList();
    }

    @Transactional(readOnly = true)
    public RegisterSessionDetailResponse getSessionDetail(UUID sessionId, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        RegisterSession session = registerSessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão", sessionId));
        Register register = registerRepository.findByIdAndTenantId(session.getRegisterId(), tenantId).orElse(null);
        String registerName = register != null ? register.getName() : null;
        User user = resolveUser(session.getUserId());
        List<Sale> sales = findSalesInSession(session);
        List<SaleResponse> saleResponses = saleService.toResponseList(sales);
        long count = sales.size();
        BigDecimal total = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return RegisterSessionDetailResponse.builder()
                .id(session.getId())
                .registerId(session.getRegisterId())
                .registerName(registerName)
                .userId(session.getUserId())
                .userFullName(user != null ? user.getFullName() : null)
                .username(user != null ? user.getUsername() : null)
                .tenantId(session.getTenantId())
                .openedAt(session.getOpenedAt())
                .closedAt(session.getClosedAt())
                .createdAt(session.getCreatedAt())
                .sales(saleResponses)
                .salesCount(count)
                .totalSales(total)
                .build();
    }

    private List<Sale> findSalesInSession(RegisterSession session) {
        LocalDateTime from = session.getOpenedAt().atZone(DEFAULT_ZONE).toLocalDateTime();
        LocalDateTime to = session.getClosedAt() != null
                ? session.getClosedAt().atZone(DEFAULT_ZONE).toLocalDateTime()
                : LocalDateTime.now(DEFAULT_ZONE);
        String registerIdStr = session.getRegisterId().toString();
        return saleRepository.findByTenantIdAndRegisterIdAndSellerIdAndSaleDateBetween(
                session.getTenantId(), registerIdStr, session.getUserId(), from, to);
    }

    private User resolveUser(UUID userId) {
        return userId != null ? userRepository.findById(userId).orElse(null) : null;
    }

    private RegisterSessionResponse toResponse(RegisterSession s, String registerName, User user, long salesCount, BigDecimal totalSales) {
        return RegisterSessionResponse.builder()
                .id(s.getId())
                .registerId(s.getRegisterId())
                .registerName(registerName)
                .userId(s.getUserId())
                .userFullName(user != null ? user.getFullName() : null)
                .username(user != null ? user.getUsername() : null)
                .tenantId(s.getTenantId())
                .openedAt(s.getOpenedAt())
                .closedAt(s.getClosedAt())
                .createdAt(s.getCreatedAt())
                .salesCount(salesCount)
                .totalSales(totalSales != null ? totalSales : BigDecimal.ZERO)
                .build();
    }

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return requestTenantId != null ? requestTenantId : SecurityUtils.getTenantIdOptional()
                    .orElseThrow(() -> new IllegalStateException("Selecione uma empresa."));
        }
        return SecurityUtils.requireTenantId();
    }
}
