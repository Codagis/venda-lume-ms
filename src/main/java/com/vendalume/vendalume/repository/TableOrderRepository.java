package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.TableOrder;
import com.vendalume.vendalume.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableOrderRepository extends JpaRepository<TableOrder, UUID> {

    @Query(value = "SELECT * FROM table_order WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<TableOrder> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM table_order WHERE tenant_id = CAST(:tenantId AS UUID) AND table_id = CAST(:tableId AS UUID) AND status = :status LIMIT 1", nativeQuery = true)
    Optional<TableOrder> findByTenantIdAndTableIdAndStatus(@Param("tenantId") UUID tenantId, @Param("tableId") UUID tableId, @Param("status") String status);

    @Query(value = "SELECT * FROM table_order WHERE tenant_id = CAST(:tenantId AS UUID) AND status = :status ORDER BY opened_at DESC", nativeQuery = true)
    List<TableOrder> findByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") String status);
}
