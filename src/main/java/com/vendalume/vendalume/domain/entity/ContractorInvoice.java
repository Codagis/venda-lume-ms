package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa ContractorInvoice no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Entity
@Table(name = "contractor_invoices", indexes = {
    @Index(name = "idx_contractor_invoice_tenant", columnList = "tenant_id"),
    @Index(name = "idx_contractor_invoice_contractor", columnList = "contractor_id"),
    @Index(name = "idx_contractor_invoice_reference", columnList = "tenant_id, reference_month")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)

public class ContractorInvoice extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Column(name = "contractor_id", nullable = false, columnDefinition = "UUID")
    private UUID contractorId;

    @Comment("Competência da NF (YYYY-MM)")
    @Column(name = "reference_month", nullable = false, length = 7)
    private String referenceMonth;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "nf_number", length = 50)
    private String nfNumber;

    @Comment("Chave da NF-e (44 dígitos)")
    @Column(name = "nf_key", length = 44)
    private String nfKey;

    @Column(name = "description", length = 500)
    private String description;

    @Comment("Caminho ou URL do arquivo no Google Cloud Storage")
    @Column(name = "file_gcs_path", length = 1024)
    private String fileGcsPath;

    @Column(name = "file_original_name", length = 255)
    private String fileOriginalName;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
