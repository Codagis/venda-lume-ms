package com.vendalume.vendalume.api.dto.sale;

import com.vendalume.vendalume.domain.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Requisição para adicionar/atualizar pagamento de venda pendente (status OPEN)")
public class SalePaymentUpdateRequest {

    @NotNull(message = "Forma de pagamento é obrigatória")
    @Schema(description = "Forma de pagamento principal")
    private PaymentMethod paymentMethod;

    @Schema(description = "Valor recebido (default: total da venda). Para dinheiro, informar valor dado pelo cliente.")
    private BigDecimal amountReceived;

    @Schema(description = "Desconto em valor (R$) sobre o subtotal")
    private BigDecimal discountAmount;

    @Schema(description = "Desconto percentual sobre o subtotal")
    private BigDecimal discountPercent;

    @Schema(description = "Taxa de entrega")
    private BigDecimal deliveryFee;

    @Schema(description = "Número de parcelas (obrigatório quando pagamento é cartão de crédito)")
    private Integer installmentsCount;

    @Schema(description = "ID da maquininha usada (cartão)")
    private UUID cardMachineId;

    @Schema(description = "Bandeira do cartão: 01=Visa, 02=Master, 03=Amex, 04=Sorocred, 99=Outros")
    private String cardBrand;

    @Schema(description = "Número da autorização da transação (para NFC-e)")
    private String cardAuthorization;

    @Schema(description = "Tipo de integração cartão: 1=TEF, 2=POS")
    private Integer cardIntegrationType;
}
