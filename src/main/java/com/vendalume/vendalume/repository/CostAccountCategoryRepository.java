package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.CostAccountCategory;
import com.vendalume.vendalume.domain.enums.CostCategoryKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CostAccountCategoryRepository extends JpaRepository<CostAccountCategory, UUID> {

    List<CostAccountCategory> findByTenantIdAndKindOrderByDisplayOrderAscNameAsc(UUID tenantId, CostCategoryKind kind);

    Optional<CostAccountCategory> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndKindAndNameIgnoreCaseAndIdNot(UUID tenantId, CostCategoryKind kind, String name, UUID id);

    boolean existsByTenantIdAndKindAndNameIgnoreCase(UUID tenantId, CostCategoryKind kind, String name);
}
