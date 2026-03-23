package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    @Query(value = "SELECT * FROM employees WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Employee> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM employees WHERE tenant_id = CAST(:tenantId AS UUID) AND active = true ORDER BY name", nativeQuery = true)
    List<Employee> findByTenantIdAndActiveTrueOrderByName(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM employees WHERE tenant_id = CAST(:tenantId AS UUID) AND document = :document AND document IS NOT NULL AND document != '')", nativeQuery = true)
    boolean existsByTenantIdAndDocument(@Param("tenantId") UUID tenantId, @Param("document") String document);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM employees WHERE tenant_id = CAST(:tenantId AS UUID) AND document = :document AND id != CAST(:excludeId AS UUID))", nativeQuery = true)
    boolean existsByTenantIdAndDocumentAndIdNot(@Param("tenantId") UUID tenantId, @Param("document") String document, @Param("excludeId") UUID excludeId);
}
