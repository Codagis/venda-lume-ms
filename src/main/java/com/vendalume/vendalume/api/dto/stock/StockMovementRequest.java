package com.vendalume.vendalume.api.dto.stock;

import com.vendalume.vendalume.domain.enums.StockMovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de requisição para movimentação de estoque.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementRequest {

    @NotNull(message = "Produto é obrigatório")
    private UUID productId;

    @NotNull(message = "Tipo de movimentação é obrigatório")
    private StockMovementType movementType;

    @NotNull(message = "Quantidade é obrigatória")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal quantity;

    @Size(max = 500)
    private String notes;
}
