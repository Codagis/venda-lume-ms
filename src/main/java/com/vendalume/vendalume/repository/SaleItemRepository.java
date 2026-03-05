package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link SaleItem}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    @Query(value = """
            SELECT * FROM sale_items
            WHERE sale_id = CAST(:saleId AS UUID)
            ORDER BY item_order ASC, id ASC
            """, nativeQuery = true)
    List<SaleItem> findBySaleIdOrderByItemOrderAsc(@Param("saleId") UUID saleId);

    @Query(value = """
            SELECT * FROM sale_items
            WHERE product_id = CAST(:productId AS UUID)
            """, nativeQuery = true)
    List<SaleItem> findByProductId(@Param("productId") UUID productId);

    /**
     * Agrega vendas por produto para um tenant e período.
     * Retorna: product_id, product_name, total_quantity, total_revenue, sale_count
     */
    @Query(value = """
            SELECT si.product_id, si.product_name,
                   COALESCE(SUM(si.quantity), 0) as total_quantity,
                   COALESCE(SUM(si.total), 0) as total_revenue,
                   COUNT(DISTINCT s.id)::bigint as sale_count
            FROM sale_items si
            JOIN sales s ON s.id = si.sale_id
            WHERE s.tenant_id = CAST(:tenantId AS UUID)
              AND s.sale_date >= :startDate AND s.sale_date <= :endDate
              AND s.status != 'CANCELLED'
            GROUP BY si.product_id, si.product_name
            ORDER BY total_quantity DESC
            """, nativeQuery = true)
    List<Object[]> findProductSalesByTenantAndPeriod(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
