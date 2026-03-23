package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.AccountPayable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link AccountPayable}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface AccountPayableRepository extends JpaRepository<AccountPayable, UUID>, JpaSpecificationExecutor<AccountPayable> {

    @Query(value = "SELECT * FROM account_payable WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<AccountPayable> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM account_payable WHERE tenant_id = CAST(:tenantId AS UUID) ORDER BY due_date ASC", nativeQuery = true)
    List<AccountPayable> findByTenantIdOrderByDueDateAsc(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM account_payable WHERE tenant_id = CAST(:tenantId AS UUID) AND employee_id = CAST(:employeeId AS UUID) AND payroll_reference = :payrollReference)", nativeQuery = true)
    boolean existsByTenantIdAndEmployeeIdAndPayrollReference(@Param("tenantId") UUID tenantId, @Param("employeeId") UUID employeeId, @Param("payrollReference") String payrollReference);

    @Query(value = "SELECT * FROM account_payable WHERE tenant_id = CAST(:tenantId AS UUID) AND payroll_reference = :payrollReference ORDER BY due_date, description", nativeQuery = true)
    List<AccountPayable> findByTenantIdAndPayrollReference(@Param("tenantId") UUID tenantId, @Param("payrollReference") String payrollReference);

    @Query(value = "SELECT DISTINCT payroll_reference FROM account_payable WHERE tenant_id = CAST(:tenantId AS UUID) AND payroll_reference IS NOT NULL AND payroll_reference != '' ORDER BY payroll_reference DESC", nativeQuery = true)
    List<String> findDistinctPayrollReferencesByTenantId(@Param("tenantId") UUID tenantId);
}
