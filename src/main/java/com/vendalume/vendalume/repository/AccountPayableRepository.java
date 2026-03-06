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
}
