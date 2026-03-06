package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.TableOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link TableOrderItem}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface TableOrderItemRepository extends JpaRepository<TableOrderItem, UUID> {

    @Query(value = """
            SELECT * FROM table_order_item
            WHERE order_id = CAST(:orderId AS UUID)
            ORDER BY item_order ASC, id ASC
            """, nativeQuery = true)
    List<TableOrderItem> findByOrderIdOrderByItemOrderAsc(@Param("orderId") UUID orderId);

    @Query(value = """
            SELECT i.* FROM table_order_item i
            INNER JOIN table_order o ON i.order_id = o.id
            WHERE i.id = CAST(:itemId AS UUID) AND o.tenant_id = CAST(:tenantId AS UUID)
            LIMIT 1
            """, nativeQuery = true)
    Optional<TableOrderItem> findByIdAndOrderTenantId(@Param("itemId") UUID itemId, @Param("tenantId") UUID tenantId);
}
