package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    @Query(value = "SELECT * FROM customers WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Customer> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM customers WHERE tenant_id = CAST(:tenantId AS UUID) AND LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND document IS NOT NULL AND document != '' LIMIT 2", nativeQuery = true)
    List<Customer> findByTenantIdAndNameIgnoreCaseAndDocumentNotNull(@Param("tenantId") UUID tenantId, @Param("name") String name);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM customers WHERE tenant_id = CAST(:tenantId AS UUID) AND document = :document)", nativeQuery = true)
    boolean existsByTenantIdAndDocument(@Param("tenantId") UUID tenantId, @Param("document") String document);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM customers WHERE tenant_id = CAST(:tenantId AS UUID) AND document = :document AND id != CAST(:excludeId AS UUID))", nativeQuery = true)
    boolean existsByTenantIdAndDocumentAndIdNot(@Param("tenantId") UUID tenantId, @Param("document") String document, @Param("excludeId") UUID excludeId);
}
