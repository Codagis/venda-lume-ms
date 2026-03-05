package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.TableSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableSectionRepository extends JpaRepository<TableSection, UUID>, JpaSpecificationExecutor<TableSection> {

    @Query(value = "SELECT * FROM table_section WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<TableSection> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM table_section WHERE tenant_id = CAST(:tenantId AS UUID) ORDER BY display_order ASC, name ASC", nativeQuery = true)
    List<TableSection> findByTenantIdOrderByDisplayOrderAscNameAsc(@Param("tenantId") UUID tenantId);
}
