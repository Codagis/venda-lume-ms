package com.vendalume.vendalume.api.dto.sale;

import com.vendalume.vendalume.domain.enums.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de resposta para item de venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item de venda")
public class SaleItemResponse {

    @Schema(description = "ID do item")
    private UUID id;

    @Schema(description = "ID do produto")
    private UUID productId;

    @Schema(description = "Nome do produto no momento da venda")
    private String productName;

    @Schema(description = "SKU do produto no momento da venda")
    private String productSku;

    @Schema(description = "Quantidade vendida")
    private BigDecimal quantity;

    @Schema(description = "Unidade de medida")
    private UnitOfMeasure unitOfMeasure;

    @Schema(description = "Preço unitário no momento da venda")
    private BigDecimal unitPrice;

    @Schema(description = "Desconto aplicado no item")
    private BigDecimal discountAmount;

    @Schema(description = "Imposto do item")
    private BigDecimal taxAmount;

    @Schema(description = "Total do item")
    private BigDecimal total;

    @Schema(description = "Observações do item")
    private String observations;
}
