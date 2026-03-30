package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.costcontrol.CostAccountCategoryRequest;
import com.vendalume.vendalume.api.dto.costcontrol.CostAccountCategoryResponse;
import com.vendalume.vendalume.domain.entity.CostAccountCategory;
import com.vendalume.vendalume.domain.enums.CostCategoryKind;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.repository.CostAccountCategoryRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CostAccountCategoryService {

    private final CostAccountCategoryRepository repository;

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (requestTenantId == null) {
                throw new IllegalArgumentException("Selecione a empresa.");
            }
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }

    @Transactional(readOnly = true)
    public List<CostAccountCategoryResponse> list(UUID requestTenantId, CostCategoryKind kind) {
        UUID tenantId = resolveTenantId(requestTenantId);
        return repository.findByTenantIdAndKindOrderByDisplayOrderAscNameAsc(tenantId, kind).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CostAccountCategoryResponse create(CostAccountCategoryRequest request) {
        UUID tenantId = resolveTenantId(request.getTenantId());
        String name = request.getName().trim();
        if (repository.existsByTenantIdAndKindAndNameIgnoreCase(tenantId, request.getKind(), name)) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome para este tipo.");
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        CostAccountCategory e = CostAccountCategory.builder()
                .tenantId(tenantId)
                .kind(request.getKind())
                .name(name)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .active(request.getActive() != null ? request.getActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        e.setCreatedBy(userId);
        e.setUpdatedBy(userId);
        e = repository.save(e);
        return toResponse(e);
    }

    @Transactional
    public CostAccountCategoryResponse update(UUID id, CostAccountCategoryRequest request) {
        UUID tenantId = resolveTenantId(request.getTenantId());
        CostAccountCategory e = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
        String name = request.getName().trim();
        if (repository.existsByTenantIdAndKindAndNameIgnoreCaseAndIdNot(tenantId, e.getKind(), name, id)) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome para este tipo.");
        }
        e.setName(name);
        e.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getActive() != null) {
            e.setActive(request.getActive());
        }
        if (request.getDisplayOrder() != null) {
            e.setDisplayOrder(request.getDisplayOrder());
        }
        e.setUpdatedBy(SecurityUtils.getCurrentUserId());
        e = repository.save(e);
        return toResponse(e);
    }

    @Transactional
    public void delete(UUID id, UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        CostAccountCategory e = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
        repository.delete(e);
    }

    private CostAccountCategoryResponse toResponse(CostAccountCategory e) {
        return CostAccountCategoryResponse.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .kind(e.getKind())
                .name(e.getName())
                .description(e.getDescription())
                .active(e.getActive())
                .displayOrder(e.getDisplayOrder())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
