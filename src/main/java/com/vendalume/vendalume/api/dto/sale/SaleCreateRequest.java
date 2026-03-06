package com.vendalume.vendalume.api.dto.sale;

import com.vendalume.vendalume.domain.enums.PaymentMethod;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de requisição para criar venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para criar venda")
public class SaleCreateRequest {

    @Schema(description = "ID da empresa. Obrigatório para root, ignorado para não-root.")
    private UUID tenantId;

    @Schema(description = "ID do caixa (PDV) da sessão em que a venda foi realizada; usado para auditoria.")
    private UUID registerId;

    @NotNull(message = "Tipo de venda é obrigatório")
    @Schema(description = "Tipo da venda (PDV, DELIVERY, etc.)")
    private SaleType saleType;

    @Valid
    @NotEmpty(message = "A venda deve ter pelo menos um item")
    @Schema(description = "Itens da venda")
    private List<SaleItemRequest> items;

    @Schema(description = "Desconto em valor (R$) sobre o subtotal")
    private BigDecimal discountAmount;

    @Schema(description = "Desconto percentual sobre o subtotal")
    @DecimalMin(value = "0", message = "Desconto percentual não pode ser negativo")
    private BigDecimal discountPercent;

    @Schema(description = "Taxa de entrega quando aplicável")
    private BigDecimal deliveryFee;

    @Schema(description = "Status da venda. Padrão: COMPLETED. Use OPEN para registrar como pendente (pagamento será adicionado depois em Consultar vendas).")
    private SaleStatus status;

    @Schema(description = "Forma de pagamento principal. Obrigatório quando status for COMPLETED; opcional quando status for OPEN.")
    private PaymentMethod paymentMethod;

    @Schema(description = "Valor recebido (para cálculo de troco)")
    private BigDecimal amountReceived;

    @Schema(description = "ID do cliente cadastrado (quando a venda é vinculada a um cliente)")
    private UUID customerId;

    @Schema(description = "Nome do cliente")
    private String customerName;

    @Schema(description = "CPF ou CNPJ do cliente")
    private String customerDocument;

    @Schema(description = "Telefone do cliente")
    private String customerPhone;

    @Schema(description = "E-mail do cliente")
    private String customerEmail;

    @Schema(description = "Observações do pedido")
    private String notes;

    @Schema(description = "Número de parcelas (obrigatório quando pagamento é cartão de crédito)")
    private Integer installmentsCount;

    @Schema(description = "ID da maquininha usada (cartão); traz CNPJ da adquirente para NFC-e")
    private UUID cardMachineId;

    @Schema(description = "Bandeira do cartão para NFC-e: 01=Visa, 02=Master, 03=Amex, 04=Sorocred, 99=Outros")
    private String cardBrand;

    @Schema(description = "Número da autorização da transação (para NFC-e)")
    private String cardAuthorization;

    @Schema(description = "Tipo de integração cartão na NFC-e: 1=TEF, 2=POS")
    private Integer cardIntegrationType;

    @Schema(description = "Endereço de entrega (para tipo DELIVERY)")
    private String deliveryAddress;

    @Schema(description = "Complemento do endereço de entrega")
    private String deliveryComplement;

    @Schema(description = "CEP do endereço de entrega")
    private String deliveryZipCode;

    @Schema(description = "Bairro do endereço de entrega")
    private String deliveryNeighborhood;

    @Schema(description = "Cidade do endereço de entrega")
    private String deliveryCity;

    @Schema(description = "UF do endereço de entrega")
    private String deliveryState;
}
