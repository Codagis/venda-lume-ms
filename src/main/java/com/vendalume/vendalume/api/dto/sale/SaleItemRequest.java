package com.vendalume.vendalume.api.dto.sale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de requisição com item de venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item de venda")
public class SaleItemRequest {

    @NotNull(message = "Produto é obrigatório")
    @Schema(description = "ID do produto")
    private UUID productId;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
    @Schema(description = "Quantidade vendida")
    private BigDecimal quantity;

    @Schema(description = "Desconto em valor (R$) aplicado no item")
    private BigDecimal discountAmount;

    @Schema(description = "Desconto percentual aplicado no item")
    private BigDecimal discountPercent;
}
