package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link StockMovement}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID>, JpaSpecificationExecutor<StockMovement> {

    @Query(value = "SELECT * FROM stock_movements WHERE product_id = CAST(:productId AS UUID) ORDER BY created_at DESC", nativeQuery = true)
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(@Param("productId") UUID productId, Pageable pageable);

    @Query(value = "SELECT * FROM stock_movements WHERE tenant_id = CAST(:tenantId AS UUID) AND product_id = CAST(:productId AS UUID) ORDER BY created_at DESC", nativeQuery = true)
    List<StockMovement> findByTenantIdAndProductIdOrderByCreatedAtDesc(@Param("tenantId") UUID tenantId, @Param("productId") UUID productId);
}
