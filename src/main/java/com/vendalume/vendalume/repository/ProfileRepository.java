package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link Profile}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    @Query(value = "SELECT * FROM profiles WHERE tenant_id = CAST(:tenantId AS UUID) ORDER BY name ASC", nativeQuery = true)
    List<Profile> findByTenantIdOrderByNameAsc(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM profiles WHERE tenant_id IS NULL ORDER BY name ASC", nativeQuery = true)
    List<Profile> findByTenantIdIsNullOrderByNameAsc();

    @Query(value = "SELECT * FROM profiles ORDER BY name ASC", nativeQuery = true)
    List<Profile> findAllByOrderByNameAsc();

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.permissions WHERE p.id = :id")
    Optional<Profile> findByIdWithPermissions(@Param("id") UUID id);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM profiles WHERE tenant_id = CAST(:tenantId AS UUID) AND name = :name)", nativeQuery = true)
    boolean existsByTenantIdAndName(@Param("tenantId") UUID tenantId, @Param("name") String name);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM profiles WHERE tenant_id = CAST(:tenantId AS UUID) AND name = :name AND id != CAST(:excludeId AS UUID))", nativeQuery = true)
    boolean existsByTenantIdAndNameAndIdNot(@Param("tenantId") UUID tenantId, @Param("name") String name, @Param("excludeId") UUID excludeId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM profiles WHERE tenant_id IS NULL AND name = :name)", nativeQuery = true)
    boolean existsByTenantIdIsNullAndName(@Param("name") String name);
}
