package com.vendalume.vendalume.api.dto.costcontrol;

import com.vendalume.vendalume.domain.enums.AccountStatus;
import com.vendalume.vendalume.domain.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de resposta com dados de conta a receber.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountReceivableResponse {

    private UUID id;
    private UUID tenantId;
    private UUID customerId;
    private String customerName;
    private UUID saleId;
    private String saleNumber;
    private String description;
    private String reference;
    private String category;
    private LocalDate dueDate;
    private BigDecimal amount;
    private BigDecimal receivedAmount;
    private AccountStatus status;
    private LocalDate receiptDate;
    private PaymentMethod paymentMethod;
    private String notes;
}
