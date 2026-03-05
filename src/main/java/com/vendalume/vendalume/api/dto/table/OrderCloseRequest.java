package com.vendalume.vendalume.api.dto.table;

import com.vendalume.vendalume.domain.enums.PaymentMethod;
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
public class OrderCloseRequest {

    @NotNull(message = "Forma de pagamento é obrigatória")
    private PaymentMethod paymentMethod;

    private BigDecimal amountReceived;
    private Integer installmentsCount;
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private UUID cardMachineId;
    private String cardBrand;
    private String cardAuthorization;
    private Integer cardIntegrationType;
    private String customerName;
    private String customerDocument;
    private String customerPhone;
    private String customerEmail;
    private String notes;
}
