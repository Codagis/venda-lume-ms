package com.vendalume.vendalume.api.dto.costcontrol;

import com.vendalume.vendalume.domain.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class PaymentRegistrationRequest {

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0", inclusive = false, message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    @NotNull(message = "Data do pagamento é obrigatória")
    private LocalDate paymentDate;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private PaymentMethod paymentMethod;
}
