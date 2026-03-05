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

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID>, JpaSpecificationExecutor<StockMovement> {

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.tenantId = :tenantId AND sm.productId = :productId ORDER BY sm.createdAt DESC")
    List<StockMovement> findByTenantIdAndProductIdOrderByCreatedAtDesc(@Param("tenantId") UUID tenantId, @Param("productId") UUID productId);
}
