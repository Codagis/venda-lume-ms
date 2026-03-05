package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID>, JpaSpecificationExecutor<Supplier> {

    @Query(value = "SELECT * FROM suppliers WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Supplier> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM suppliers WHERE tenant_id = CAST(:tenantId AS UUID) AND document = :document)", nativeQuery = true)
    boolean existsByTenantIdAndDocument(@Param("tenantId") UUID tenantId, @Param("document") String document);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM suppliers WHERE tenant_id = CAST(:tenantId AS UUID) AND document = :document AND id != CAST(:excludeId AS UUID))", nativeQuery = true)
    boolean existsByTenantIdAndDocumentAndIdNot(@Param("tenantId") UUID tenantId, @Param("document") String document, @Param("excludeId") UUID excludeId);
}
