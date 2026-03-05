package com.vendalume.vendalume.api.dto.costcontrol;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountReceivableUpdateRequest {

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 255)
    private String description;

    @Size(max = 100)
    private String reference;

    @Size(max = 50)
    private String category;

    @NotNull(message = "Data de vencimento é obrigatória")
    private LocalDate dueDate;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0", inclusive = false, message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    private UUID customerId;
    private UUID saleId;

    private String notes;
}
