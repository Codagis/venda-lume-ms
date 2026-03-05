package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.SaleAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SaleAuditRepository extends JpaRepository<SaleAudit, UUID> {

    List<SaleAudit> findBySaleIdOrderByOccurredAtDesc(UUID saleId);
}
