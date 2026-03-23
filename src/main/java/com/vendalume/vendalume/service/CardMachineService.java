package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.cardmachine.CardMachineRequest;
import com.vendalume.vendalume.api.dto.cardmachine.CardMachineResponse;
import com.vendalume.vendalume.domain.entity.CardMachine;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.repository.CardMachineRepository;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço de gestão de maquininhas de cartão.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class CardMachineService {

    private final CardMachineRepository cardMachineRepository;
    private final TenantRepository tenantRepository;

    private UUID resolveTenantId(UUID tenantId) {
        if (SecurityUtils.isCurrentUserRoot() && tenantId != null) {
            if (!tenantRepository.existsById(tenantId)) {
                throw new ResourceNotFoundException("Empresa", tenantId);
            }
            return tenantId;
        }
        return SecurityUtils.requireTenantId();
    }

    @Transactional(readOnly = true)
    public List<CardMachineResponse> listByTenant(UUID tenantId) {
        UUID resolved = resolveTenantId(tenantId);
        return cardMachineRepository.findByTenantIdOrderByIsDefaultDescNameAsc(resolved)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CardMachineResponse> listActiveByTenant(UUID tenantId) {
        UUID resolved = resolveTenantId(tenantId);
        return cardMachineRepository.findByTenantIdAndActiveTrueOrderByIsDefaultDescNameAsc(resolved)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CardMachineResponse findById(UUID tenantId, UUID id) {
        UUID resolved = resolveTenantId(tenantId);
        CardMachine m = cardMachineRepository.findByIdAndTenantId(id, resolved)
                .orElseThrow(() -> new ResourceNotFoundException("Maquininha", id));
        return toResponse(m);
    }

    @Transactional
    public CardMachineResponse create(UUID tenantId, CardMachineRequest request) {
        UUID resolved = resolveTenantId(tenantId);
        String feeType = request.getFeeType() != null ? request.getFeeType().trim().toUpperCase() : null;
        if (!"PERCENTAGE".equals(feeType) && !"FIXED_AMOUNT".equals(feeType)) {
            throw new IllegalArgumentException("feeType deve ser PERCENTAGE ou FIXED_AMOUNT");
        }
        if (cardMachineRepository.existsByTenantIdAndNameIgnoreCase(resolved, request.getName().trim())) {
            throw new IllegalArgumentException("Ja existe maquininha com este nome");
        }
        String acquirerCnpj = request.getAcquirerCnpj() != null ? request.getAcquirerCnpj().replaceAll("\\D", "") : null;
        if (acquirerCnpj != null && acquirerCnpj.length() != 14) acquirerCnpj = null;
        CardMachine m = CardMachine.builder()
                .tenantId(resolved)
                .name(request.getName().trim())
                .feeType(feeType)
                .feeValue(request.getFeeValue())
                .acquirerCnpj(acquirerCnpj)
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .active(request.getActive() != null ? request.getActive() : true)
                .maxInstallments(request.getMaxInstallments())
                .maxInstallmentsNoInterest(request.getMaxInstallmentsNoInterest())
                .interestRatePercent(request.getInterestRatePercent())
                .build();
        if (Boolean.TRUE.equals(m.getIsDefault())) {
            cardMachineRepository.findByTenantIdOrderByIsDefaultDescNameAsc(resolved)
                    .forEach(c -> c.setIsDefault(false));
            cardMachineRepository.flush();
        }
        m = cardMachineRepository.save(m);
        return toResponse(m);
    }

    @Transactional
    public CardMachineResponse update(UUID tenantId, UUID id, CardMachineRequest request) {
        UUID resolved = resolveTenantId(tenantId);
        CardMachine m = cardMachineRepository.findByIdAndTenantId(id, resolved)
                .orElseThrow(() -> new ResourceNotFoundException("Maquininha", id));
        String feeType = request.getFeeType() != null ? request.getFeeType().trim().toUpperCase() : null;
        if (!"PERCENTAGE".equals(feeType) && !"FIXED_AMOUNT".equals(feeType)) {
            throw new IllegalArgumentException("feeType deve ser PERCENTAGE ou FIXED_AMOUNT");
        }
        if (cardMachineRepository.existsByTenantIdAndNameIgnoreCaseAndIdNot(resolved, request.getName().trim(), id)) {
            throw new IllegalArgumentException("Ja existe maquininha com este nome");
        }
        m.setName(request.getName().trim());
        m.setFeeType(feeType);
        m.setFeeValue(request.getFeeValue());
        String acquirerCnpj = request.getAcquirerCnpj() != null ? request.getAcquirerCnpj().replaceAll("\\D", "") : null;
        m.setAcquirerCnpj((acquirerCnpj != null && acquirerCnpj.length() == 14) ? acquirerCnpj : null);
        m.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        if (request.getActive() != null) m.setActive(request.getActive());
        m.setMaxInstallments(request.getMaxInstallments());
        m.setMaxInstallmentsNoInterest(request.getMaxInstallmentsNoInterest());
        m.setInterestRatePercent(request.getInterestRatePercent());
        if (Boolean.TRUE.equals(m.getIsDefault())) {
            cardMachineRepository.findByTenantIdOrderByIsDefaultDescNameAsc(resolved)
                    .stream()
                    .filter(c -> !c.getId().equals(id))
                    .forEach(c -> c.setIsDefault(false));
            cardMachineRepository.flush();
        }
        m = cardMachineRepository.save(m);
        return toResponse(m);
    }

    @Transactional
    public void delete(UUID tenantId, UUID id) {
        UUID resolved = resolveTenantId(tenantId);
        CardMachine m = cardMachineRepository.findByIdAndTenantId(id, resolved)
                .orElseThrow(() -> new ResourceNotFoundException("Maquininha", id));
        cardMachineRepository.delete(m);
    }

    private CardMachineResponse toResponse(CardMachine m) {
        return CardMachineResponse.builder()
                .id(m.getId())
                .tenantId(m.getTenantId())
                .name(m.getName())
                .feeType(m.getFeeType())
                .feeValue(m.getFeeValue())
                .acquirerCnpj(m.getAcquirerCnpj())
                .isDefault(m.getIsDefault())
                .active(m.getActive())
                .maxInstallments(m.getMaxInstallments())
                .maxInstallmentsNoInterest(m.getMaxInstallmentsNoInterest())
                .interestRatePercent(m.getInterestRatePercent())
                .build();
    }
}
