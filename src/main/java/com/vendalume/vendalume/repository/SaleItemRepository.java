package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
