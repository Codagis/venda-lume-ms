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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountPayableResponse {

    private UUID id;
    private UUID tenantId;
    private UUID supplierId;
    private String supplierName;
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
