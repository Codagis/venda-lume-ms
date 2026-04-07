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
 * Entidade que representa AccountPayable no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Entity
@Table(name = "account_payable", indexes = {
    @Index(name = "idx_ap_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_ap_tenant_due_date", columnList = "tenant_id, due_date"),
    @Index(name = "idx_ap_supplier", columnList = "supplier_id"),
    @Index(name = "idx_ap_employee", columnList = "employee_id"),
    @Index(name = "idx_ap_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)

public class AccountPayable extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID do tenant")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Fornecedor vinculado")
    @Column(name = "supplier_id", columnDefinition = "UUID")
    private UUID supplierId;

    @Comment("Funcionário vinculado (conta de salário/folha)")
    @Column(name = "employee_id", columnDefinition = "UUID")
    private UUID employeeId;

    @Comment("Prestador PJ vinculado (pagamento de serviço)")
    @Column(name = "contractor_id", columnDefinition = "UUID")
    private UUID contractorId;

    @Comment("Nota fiscal do prestador vinculada ao pagamento")
    @Column(name = "contractor_invoice_id", columnDefinition = "UUID")
    private UUID contractorInvoiceId;

    @Comment("Mês de referência da folha (YYYY-MM) para contas geradas por funcionário")
    @Column(name = "payroll_reference", length = 7)
    private String payrollReference;

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

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

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
