package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.SaleAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link SaleAudit}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface SaleAuditRepository extends JpaRepository<SaleAudit, UUID> {

    @Query(value = "SELECT * FROM sale_audit WHERE sale_id = CAST(:saleId AS UUID) ORDER BY occurred_at DESC", nativeQuery = true)
    List<SaleAudit> findBySaleIdOrderByOccurredAtDesc(@Param("saleId") UUID saleId);
}
