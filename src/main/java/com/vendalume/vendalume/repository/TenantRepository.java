package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link Tenant}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    @Query(value = "SELECT * FROM tenants ORDER BY name ASC", nativeQuery = true)
    List<Tenant> findAllByOrderByNameAsc();

    @Query(value = "SELECT * FROM tenants WHERE active = true ORDER BY name ASC", nativeQuery = true)
    List<Tenant> findByActiveTrueOrderByNameAsc();

    @Query(value = "SELECT * FROM tenants WHERE document = :document LIMIT 1", nativeQuery = true)
    Optional<Tenant> findByDocument(@Param("document") String document);

    @Query(value = "SELECT * FROM tenants WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1", nativeQuery = true)
    Optional<Tenant> findByNameIgnoreCase(@Param("name") String name);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM tenants WHERE document = :document)", nativeQuery = true)
    boolean existsByDocument(@Param("document") String document);
}
