package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.register.AssignOperatorsRequest;
import com.vendalume.vendalume.api.dto.register.CashierOption;
import com.vendalume.vendalume.api.dto.register.RegisterRequest;
import com.vendalume.vendalume.api.dto.register.RegisterResponse;
import com.vendalume.vendalume.domain.entity.Register;
import com.vendalume.vendalume.domain.entity.RegisterOperator;
import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.domain.enums.UserRole;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.repository.RegisterOperatorRepository;
import com.vendalume.vendalume.repository.RegisterRepository;
import com.vendalume.vendalume.repository.UserRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de gestão de pontos de venda (caixas).
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class RegisterService {

    private static final Set<UserRole> CASHIER_ROLES = Set.of(UserRole.CASHIER, UserRole.OPERATOR);

    private final RegisterRepository registerRepository;
    private final RegisterOperatorRepository registerOperatorRepository;
    private final UserRepository userRepository;

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return requestTenantId != null ? requestTenantId : SecurityUtils.getTenantIdOptional()
                    .orElseThrow(() -> new IllegalStateException("Selecione uma empresa."));
        }
        return SecurityUtils.requireTenantId();
    }

    @Transactional(readOnly = true)
    public List<RegisterResponse> listByTenant(UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        List<Register> list = registerRepository.findByTenantIdOrderByName(tenantId);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RegisterResponse> listActiveByTenant(UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        List<Register> list = registerRepository.findByTenantIdAndActiveTrueOrderByName(tenantId);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RegisterResponse getById(UUID id, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        Register r = registerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de venda", id));
        return toResponse(r);
    }

    @Transactional
    public RegisterResponse create(RegisterRequest request, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        if (registerRepository.existsByTenantIdAndName(tenantId, request.getName().trim())) {
            throw new IllegalArgumentException("Já existe um caixa com este nome nesta empresa.");
        }
        UUID userId = SecurityUtils.getCurrentUserId();

        Register r = Register.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .name(request.getName().trim())
                .code(request.getCode() != null && !request.getCode().isBlank() ? request.getCode().trim() : null)
                .equipmentType(request.getEquipmentType())
                .description(request.getDescription() != null && !request.getDescription().isBlank() ? request.getDescription().trim() : null)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        r.setCreatedBy(userId);
        r.setUpdatedBy(userId);
        r = registerRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public RegisterResponse update(UUID id, RegisterRequest request, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        Register r = registerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de venda", id));
        if (registerRepository.existsByTenantIdAndNameAndIdNot(tenantId, request.getName().trim(), id)) {
            throw new IllegalArgumentException("Já existe outro caixa com este nome nesta empresa.");
        }
        UUID userId = SecurityUtils.getCurrentUserId();

        r.setName(request.getName().trim());
        r.setCode(request.getCode() != null && !request.getCode().isBlank() ? request.getCode().trim() : null);
        r.setEquipmentType(request.getEquipmentType());
        r.setDescription(request.getDescription() != null && !request.getDescription().isBlank() ? request.getDescription().trim() : null);
        r.setActive(request.getActive() != null ? request.getActive() : true);
        r.setUpdatedBy(userId);
        r = registerRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public void delete(UUID id, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        Register r = registerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de venda", id));
        registerRepository.delete(r);
    }

    @Transactional
    public RegisterResponse assignOperators(UUID registerId, AssignOperatorsRequest request, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        Register r = registerRepository.findByIdAndTenantId(registerId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de venda", registerId));

        List<UUID> userIds = request.getUserIds() != null ? request.getUserIds().stream().distinct().toList() : List.of();
        List<User> users = userRepository.findAllById(userIds);
        Set<UUID> validUserIds = users.stream()
                .filter(u -> tenantId.equals(u.getTenantId()))
                .filter(u -> CASHIER_ROLES.contains(u.getRole()))
                .map(User::getId)
                .collect(Collectors.toSet());

        registerOperatorRepository.deleteByRegisterId(registerId);
        for (UUID uid : validUserIds) {
            RegisterOperator ro = RegisterOperator.builder()
                    .registerId(registerId)
                    .userId(uid)
                    .build();
            registerOperatorRepository.save(ro);
        }
        return getById(registerId, requestTenantId);
    }

    @Transactional(readOnly = true)
    public List<CashierOption> listCashiersByTenant(UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        List<User> users = userRepository.findByTenantIdAndRoleInAndActiveTrueOrderByFullNameAsc(tenantId, CASHIER_ROLES);
        return users.stream()
                .map(u -> CashierOption.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .username(u.getUsername())
                        .role(u.getRole() != null ? u.getRole().name() : null)
                        .build())
                .toList();
    }

    private RegisterResponse toResponse(Register r) {
        List<RegisterResponse.RegisterOperatorItem> operators = new ArrayList<>();
        List<RegisterOperator> opList = registerOperatorRepository.findByRegisterIdOrderByCreatedAtAsc(r.getId());
        if (!opList.isEmpty()) {
            List<UUID> userIds = opList.stream().map(RegisterOperator::getUserId).distinct().toList();
            Map<UUID, User> userMap = userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
            for (RegisterOperator op : opList) {
                User u = userMap.get(op.getUserId());
                if (u != null) {
                    operators.add(RegisterResponse.RegisterOperatorItem.builder()
                            .userId(u.getId())
                            .fullName(u.getFullName())
                            .username(u.getUsername())
                            .build());
                }
            }
        }
        return RegisterResponse.builder()
                .id(r.getId())
                .tenantId(r.getTenantId())
                .name(r.getName())
                .code(r.getCode())
                .equipmentType(r.getEquipmentType())
                .description(r.getDescription())
                .active(r.getActive())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .operators(operators)
                .build();
    }
}
