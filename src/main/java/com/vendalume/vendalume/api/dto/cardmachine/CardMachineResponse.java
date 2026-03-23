package com.vendalume.vendalume.api.dto.cardmachine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de resposta com dados da maquininha de cartão.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Maquininha de cartão")
public class CardMachineResponse {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "ID da empresa")
    private UUID tenantId;

    @Schema(description = "Nome")
    private String name;

    @Schema(description = "PERCENTAGE ou FIXED_AMOUNT")
    private String feeType;

    @Schema(description = "Valor da taxa")
    private BigDecimal feeValue;

    @Schema(description = "CNPJ da adquirente (para NFC-e)")
    private String acquirerCnpj;

    @Schema(description = "É a padrão")
    private Boolean isDefault;

    @Schema(description = "Ativa")
    private Boolean active;

    @Schema(description = "Quantidade máxima de parcelas")
    private Integer maxInstallments;

    @Schema(description = "Quantidade de parcelas sem juros")
    private Integer maxInstallmentsNoInterest;

    @Schema(description = "Taxa de juros ao mês (%)")
    private BigDecimal interestRatePercent;
}
