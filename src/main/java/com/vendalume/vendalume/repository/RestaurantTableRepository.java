package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.RestaurantTable;
import com.vendalume.vendalume.domain.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link RestaurantTable}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, UUID>, JpaSpecificationExecutor<RestaurantTable> {

    @Query(value = "SELECT * FROM restaurant_table WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<RestaurantTable> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM restaurant_table WHERE tenant_id = CAST(:tenantId AS UUID) AND active = true ORDER BY name ASC", nativeQuery = true)
    List<RestaurantTable> findByTenantIdAndActiveTrueOrderByNameAsc(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM restaurant_table WHERE tenant_id = CAST(:tenantId AS UUID) AND section_id = CAST(:sectionId AS UUID) ORDER BY name ASC", nativeQuery = true)
    List<RestaurantTable> findByTenantIdAndSectionIdOrderByNameAsc(@Param("tenantId") UUID tenantId, @Param("sectionId") UUID sectionId);

    @Query(value = "SELECT COUNT(*) FROM restaurant_table WHERE section_id = CAST(:sectionId AS UUID)", nativeQuery = true)
    long countBySectionId(@Param("sectionId") UUID sectionId);
}
