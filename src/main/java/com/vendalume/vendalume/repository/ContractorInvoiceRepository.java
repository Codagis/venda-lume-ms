package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.ContractorInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório Spring Data JPA para ContractorInvoiceRepository.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Repository
public interface ContractorInvoiceRepository extends JpaRepository<ContractorInvoice, UUID> {

    List<ContractorInvoice> findByTenantIdAndContractorIdOrderByReferenceMonthDesc(UUID tenantId, UUID contractorId);

    @Query(value = "SELECT * FROM contractor_invoices WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<ContractorInvoice> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
