package com.vendalume.vendalume.api.dto.table;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemAddRequest {

    @NotNull(message = "ID do produto é obrigatório")
    private UUID productId;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantity;
}
