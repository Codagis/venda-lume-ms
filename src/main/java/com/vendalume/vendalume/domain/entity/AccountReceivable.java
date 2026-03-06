package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.AccountStatus;
import com.vendalume.vendalume.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade que representa uma conta a receber no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-18
 */
@Entity
@Table(name = "account_receivable", indexes = {
    @Index(name = "idx_ar_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_ar_tenant_due_date", columnList = "tenant_id, due_date"),
    @Index(name = "idx_ar_customer", columnList = "customer_id"),
    @Index(name = "idx_ar_sale", columnList = "sale_id"),
    @Index(name = "idx_ar_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class AccountReceivable extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID do tenant")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Cliente vinculado")
    @Column(name = "customer_id", columnDefinition = "UUID")
    private UUID customerId;

    @Comment("Venda vinculada")
    @Column(name = "sale_id", columnDefinition = "UUID")
    private UUID saleId;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "received_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal receivedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.PENDING;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
