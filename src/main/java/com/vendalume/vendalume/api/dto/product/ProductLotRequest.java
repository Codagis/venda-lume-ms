package com.vendalume.vendalume.api.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLotRequest {

    @NotBlank(message = "Código do lote é obrigatório")
    @Size(max = 60)
    private String lotCode;

    private LocalDate expiresAt;

    @DecimalMin(value = "0", message = "Quantidade do lote não pode ser negativa")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal quantity;
}

