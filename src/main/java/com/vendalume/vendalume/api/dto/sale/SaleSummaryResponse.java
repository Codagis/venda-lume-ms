package com.vendalume.vendalume.api.dto.sale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
