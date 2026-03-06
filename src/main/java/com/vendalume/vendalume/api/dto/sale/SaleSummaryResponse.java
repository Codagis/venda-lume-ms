package com.vendalume.vendalume.api.dto.sale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de resposta com resumo de vendas do período.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumo de vendas do período")
public class SaleSummaryResponse {

    @Schema(description = "Quantidade de vendas")
    private long count;

    @Schema(description = "Valor total das vendas")
    private BigDecimal totalAmount;

    @Schema(description = "Subtotal (antes de descontos)")
    private BigDecimal subtotalAmount;

    @Schema(description = "Total de descontos")
    private BigDecimal discountAmount;

    @Schema(description = "Total de impostos")
    private BigDecimal taxAmount;

    @Schema(description = "Taxa de entrega total")
    private BigDecimal deliveryFeeAmount;
}
