package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório Spring Data JPA para ContractorRepository.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Repository
public interface ContractorRepository extends JpaRepository<Contractor, UUID>, JpaSpecificationExecutor<Contractor> {

    @Query(value = "SELECT * FROM contractors WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Contractor> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM contractors WHERE tenant_id = CAST(:tenantId AS UUID) AND active = true ORDER BY name", nativeQuery = true)
    List<Contractor> findByTenantIdAndActiveTrueOrderByName(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM contractors WHERE tenant_id = CAST(:tenantId AS UUID) AND cnpj = :cnpj AND cnpj IS NOT NULL AND cnpj != '')", nativeQuery = true)
    boolean existsByTenantIdAndCnpj(@Param("tenantId") UUID tenantId, @Param("cnpj") String cnpj);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM contractors WHERE tenant_id = CAST(:tenantId AS UUID) AND cnpj = :cnpj AND id != CAST(:excludeId AS UUID))", nativeQuery = true)
    boolean existsByTenantIdAndCnpjAndIdNot(@Param("tenantId") UUID tenantId, @Param("cnpj") String cnpj, @Param("excludeId") UUID excludeId);
}
