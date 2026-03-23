package com.vendalume.vendalume.api.dto.contractor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractorInvoiceResponse {

    private UUID id;
    private UUID tenantId;
    private UUID contractorId;
    private String contractorName;
    private String referenceMonth;
    private BigDecimal amount;
    private String nfNumber;
    private String nfKey;
    private String description;
    private String fileGcsPath;
    private String fileOriginalName;
    private Instant uploadedAt;
    private Instant createdAt;
}
