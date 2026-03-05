package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.AccountReceivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountReceivableRepository extends JpaRepository<AccountReceivable, UUID>, JpaSpecificationExecutor<AccountReceivable> {

    @Query(value = "SELECT * FROM account_receivable WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<AccountReceivable> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    List<AccountReceivable> findByTenantIdOrderByDueDateAsc(UUID tenantId);
}
