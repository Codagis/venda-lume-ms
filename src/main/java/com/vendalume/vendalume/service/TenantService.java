package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.tenant.TenantRequest;
import com.vendalume.vendalume.api.dto.tenant.TenantResponse;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    private void requireRoot() {
        if (!SecurityUtils.isCurrentUserRoot()) {
            throw new IllegalStateException("Acesso negado. Apenas usuário root pode gerenciar empresas.");
        }
    }

    @Transactional(readOnly = true)
    public List<TenantResponse> listAll() {
        requireRoot();
        return tenantRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantResponse findById(UUID id) {
        requireRoot();
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
        return toResponse(tenant);
    }

    @Transactional
    public TenantResponse create(TenantRequest request) {
        requireRoot();
        if (request.getDocument() != null && !request.getDocument().isBlank()
                && tenantRepository.existsByDocument(request.getDocument().trim())) {
            throw new IllegalArgumentException("Documento já cadastrado: " + request.getDocument());
        }
        Tenant tenant = Tenant.builder()
                .name(request.getName().trim())
                .tradeName(request.getTradeName() != null ? request.getTradeName().trim() : null)
                .document(request.getDocument() != null ? request.getDocument().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        tenant = tenantRepository.save(tenant);
        return toResponse(tenant);
    }

    @Transactional
    public TenantResponse update(UUID id, TenantRequest request) {
        requireRoot();
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
        if (request.getDocument() != null && !request.getDocument().isBlank()
                && !request.getDocument().trim().equals(tenant.getDocument())
                && tenantRepository.existsByDocument(request.getDocument().trim())) {
            throw new IllegalArgumentException("Documento já cadastrado: " + request.getDocument());
        }
        tenant.setName(request.getName().trim());
        tenant.setTradeName(request.getTradeName() != null ? request.getTradeName().trim() : null);
        tenant.setDocument(request.getDocument() != null ? request.getDocument().trim() : null);
        tenant.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        tenant.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        if (request.getActive() != null) {
            tenant.setActive(request.getActive());
        }
        tenant = tenantRepository.save(tenant);
        return toResponse(tenant);
    }

    @Transactional
    public void delete(UUID id) {
        requireRoot();
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
        tenantRepository.delete(tenant);
    }

    private TenantResponse toResponse(Tenant t) {
        return TenantResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .tradeName(t.getTradeName())
                .document(t.getDocument())
                .email(t.getEmail())
                .phone(t.getPhone())
                .active(t.getActive())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
