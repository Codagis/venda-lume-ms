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

/**
 * DTO de requisição para criar conta a pagar.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountPayableCreateRequest {

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

    private UUID supplierId;

    private UUID tenantId;

    private String notes;
}
