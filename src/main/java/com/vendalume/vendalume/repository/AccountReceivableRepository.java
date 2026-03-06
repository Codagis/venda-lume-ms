package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.AccountReceivable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link AccountReceivable}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link AccountReceivable}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface AccountReceivableRepository extends JpaRepository<AccountReceivable, UUID>, JpaSpecificationExecutor<AccountReceivable> {

    @Query(value = "SELECT * FROM account_receivable WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<AccountReceivable> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM account_receivable WHERE tenant_id = CAST(:tenantId AS UUID) ORDER BY due_date ASC", nativeQuery = true)
    List<AccountReceivable> findByTenantIdOrderByDueDateAsc(@Param("tenantId") UUID tenantId);
}
