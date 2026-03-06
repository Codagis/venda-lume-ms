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

    @Query(value = "SELECT * FROM registers WHERE imei = :imei LIMIT 1", nativeQuery = true)
    Optional<Register> findByImei(@Param("imei") String imei);

    @Query(value = "SELECT * FROM registers WHERE imei = :imei AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Register> findByImeiAndTenantId(@Param("imei") String imei, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM registers WHERE imei = :imei)", nativeQuery = true)
    boolean existsByImei(@Param("imei") String imei);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM registers WHERE imei = :imei AND id != CAST(:id AS UUID))", nativeQuery = true)
    boolean existsByImeiAndIdNot(@Param("imei") String imei, @Param("id") UUID id);

    /** PDVs ativos do tenant que o usuário está autorizado a operar (register_operators). */
    @Query(value = "SELECT r.* FROM registers r INNER JOIN register_operators ro ON r.id = ro.register_id WHERE r.tenant_id = CAST(:tenantId AS UUID) AND r.active = true AND ro.user_id = CAST(:userId AS UUID) ORDER BY r.name ASC", nativeQuery = true)
    List<Register> findActiveByTenantIdAndOperatorUserId(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId);
}
