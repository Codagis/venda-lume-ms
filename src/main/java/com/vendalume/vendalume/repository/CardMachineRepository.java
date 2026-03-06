package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.CardMachine;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link CardMachine}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link CardMachine}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface CardMachineRepository extends JpaRepository<CardMachine, UUID> {

    @Query(value = "SELECT * FROM card_machines WHERE tenant_id = CAST(:tenantId AS UUID) AND active = true ORDER BY is_default DESC, name ASC", nativeQuery = true)
    List<CardMachine> findByTenantIdAndActiveTrueOrderByIsDefaultDescNameAsc(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM card_machines WHERE tenant_id = CAST(:tenantId AS UUID) ORDER BY is_default DESC, name ASC", nativeQuery = true)
    List<CardMachine> findByTenantIdOrderByIsDefaultDescNameAsc(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM card_machines WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<CardMachine> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM card_machines WHERE tenant_id = CAST(:tenantId AS UUID) AND LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND id != CAST(:excludeId AS UUID))", nativeQuery = true)
    boolean existsByTenantIdAndNameIgnoreCaseAndIdNot(@Param("tenantId") UUID tenantId, @Param("name") String name, @Param("excludeId") UUID excludeId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM card_machines WHERE tenant_id = CAST(:tenantId AS UUID) AND LOWER(TRIM(name)) = LOWER(TRIM(:name)))", nativeQuery = true)
    boolean existsByTenantIdAndNameIgnoreCase(@Param("tenantId") UUID tenantId, @Param("name") String name);
}
