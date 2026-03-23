package com.vendalume.vendalume.api.dto.costcontrol;

import com.vendalume.vendalume.domain.enums.AccountStatus;
import com.vendalume.vendalume.domain.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de resposta com dados de conta a pagar.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountPayableResponse {

    private UUID id;
    private UUID tenantId;
    private UUID supplierId;
    private String supplierName;
    private UUID employeeId;
    private String employeeName;
    private String payrollReference;
    private UUID contractorId;
    private String contractorName;
    private UUID contractorInvoiceId;
    /** Indica se a NF do prestador PJ desta conta está anexada (obrigatório para registrar pagamento). */
    private Boolean contractorInvoiceHasFile;
    private String description;
    private String reference;
    private String category;
    private LocalDate dueDate;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private AccountStatus status;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
