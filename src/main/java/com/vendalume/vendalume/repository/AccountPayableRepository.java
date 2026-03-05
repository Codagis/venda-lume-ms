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

@Repository
public interface AccountPayableRepository extends JpaRepository<AccountPayable, UUID>, JpaSpecificationExecutor<AccountPayable> {

    @Query(value = "SELECT * FROM account_payable WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<AccountPayable> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    List<AccountPayable> findByTenantIdOrderByDueDateAsc(UUID tenantId);
}
