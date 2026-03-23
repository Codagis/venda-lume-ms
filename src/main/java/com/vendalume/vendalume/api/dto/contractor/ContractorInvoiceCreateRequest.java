package com.vendalume.vendalume.api.dto.contractor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractorInvoiceCreateRequest {

    @NotBlank(message = "Competência é obrigatória (YYYY-MM)")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "Competência deve ser YYYY-MM")
    private String referenceMonth;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0", inclusive = false, message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    @Size(max = 50)
    private String nfNumber;

    @Size(max = 44, message = "Chave NF-e possui 44 dígitos")
    private String nfKey;

    @Size(max = 500)
    private String description;
}
