package com.vendalume.vendalume.api.dto.cardmachine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Dados da maquininha")
public class CardMachineRequest {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 100)
    @Schema(description = "Nome da maquininha (ex: Cielo, Rede)")
    private String name;

    @NotBlank(message = "Tipo da taxa e obrigatorio")
    @Size(max = 20)
    @Schema(description = "PERCENTAGE ou FIXED_AMOUNT")
    private String feeType;

    @NotNull(message = "Valor da taxa e obrigatorio")
    @DecimalMin(value = "0", message = "Valor nao pode ser negativo")
    @Schema(description = "Percentual (ex: 2.5) ou valor fixo em reais (ex: 0.50)")
    private BigDecimal feeValue;

    @Size(max = 14)
    @Schema(description = "CNPJ da adquirente (somente números) para envio na NFC-e quando pagamento for cartão")
    private String acquirerCnpj;

    @Schema(description = "Maquininha padrao ao pagar com cartao")
    private Boolean isDefault;

    @Schema(description = "Ativa")
    private Boolean active;
}
