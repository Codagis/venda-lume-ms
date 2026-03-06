package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link Register}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface RegisterRepository extends JpaRepository<Register, UUID> {

    @Query(value = "SELECT * FROM registers WHERE tenant_id = CAST(:tenantId AS UUID) ORDER BY name ASC", nativeQuery = true)
    List<Register> findByTenantIdOrderByName(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM registers WHERE tenant_id = CAST(:tenantId AS UUID) AND active = true ORDER BY name ASC", nativeQuery = true)
    List<Register> findByTenantIdAndActiveTrueOrderByName(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM registers WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Register> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM registers WHERE tenant_id = CAST(:tenantId AS UUID) AND name = :name)", nativeQuery = true)
    boolean existsByTenantIdAndName(@Param("tenantId") UUID tenantId, @Param("name") String name);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM registers WHERE tenant_id = CAST(:tenantId AS UUID) AND name = :name AND id != CAST(:id AS UUID))", nativeQuery = true)
    boolean existsByTenantIdAndNameAndIdNot(@Param("tenantId") UUID tenantId, @Param("name") String name, @Param("id") UUID id);
}
