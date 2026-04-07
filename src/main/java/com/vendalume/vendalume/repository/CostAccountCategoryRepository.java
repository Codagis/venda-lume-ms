package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.CostAccountCategory;
import com.vendalume.vendalume.domain.enums.CostCategoryKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório Spring Data JPA para CostAccountCategoryRepository.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

public interface CostAccountCategoryRepository extends JpaRepository<CostAccountCategory, UUID> {

    List<CostAccountCategory> findByTenantIdAndKindOrderByDisplayOrderAscNameAsc(UUID tenantId, CostCategoryKind kind);

    Optional<CostAccountCategory> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndKindAndNameIgnoreCaseAndIdNot(UUID tenantId, CostCategoryKind kind, String name, UUID id);

    boolean existsByTenantIdAndKindAndNameIgnoreCase(UUID tenantId, CostCategoryKind kind, String name);
}
