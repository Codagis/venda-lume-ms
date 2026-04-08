package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.ProductLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductLotRepository extends JpaRepository<ProductLot, UUID> {

    List<ProductLot> findByTenantIdAndProductIdOrderByExpiresAtAscLotCodeAsc(UUID tenantId, UUID productId);

    void deleteByTenantIdAndProductId(UUID tenantId, UUID productId);
}

