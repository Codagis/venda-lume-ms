package com.vendalume.vendalume.api.dto.sale;

import com.vendalume.vendalume.domain.enums.PaymentMethod;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta com dados da venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-18
 */
/**
 * DTO de resposta com dados da venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados da venda")
public class SaleResponse {

    @Schema(description = "ID da venda")
    private UUID id;

    @Schema(description = "ID da empresa")
    private UUID tenantId;

    @Schema(description = "Número da venda")
    private String saleNumber;

    @Schema(description = "Data e hora da venda")
    private LocalDateTime saleDate;

    @Schema(description = "Status da venda")
    private SaleStatus status;

    @Schema(description = "Tipo da venda")
    private SaleType saleType;

    @Schema(description = "Nome do cliente")
    private String customerName;

    @Schema(description = "CPF ou CNPJ do cliente")
    private String customerDocument;

    @Schema(description = "Telefone do cliente")
    private String customerPhone;

    @Schema(description = "E-mail do cliente")
    private String customerEmail;

    @Schema(description = "Endereço completo de entrega ou do cliente")
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

    @Schema(description = "ID da maquininha usada (cartão)")
    private UUID cardMachineId;

    @Schema(description = "Nome da maquininha (ex: Cielo, Rede)")
    private String cardMachineName;

    @Schema(description = "Chave da NFC-e/NF-e quando emitida")
    private String invoiceKey;

    @Schema(description = "Número da NFC-e/NF-e quando emitida")
    private String invoiceNumber;

    @Schema(description = "Subtotal")
    private BigDecimal subtotal;

    @Schema(description = "Valor de desconto")
    private BigDecimal discountAmount;

    @Schema(description = "Percentual de desconto")
    private BigDecimal discountPercent;

    @Schema(description = "Impostos")
    private BigDecimal taxAmount;

    @Schema(description = "Taxa de entrega")
    private BigDecimal deliveryFee;

    @Schema(description = "Valor total")
    private BigDecimal total;

    @Schema(description = "Valor pago")
    private BigDecimal amountPaid;

    @Schema(description = "Troco")
    private BigDecimal changeAmount;

    @Schema(description = "Forma de pagamento")
    private PaymentMethod paymentMethod;

    @Schema(description = "Número de parcelas (cartão de crédito)")
    private Integer installmentsCount;

    @Schema(description = "Bandeira do cartão para NFC-e: 01=Visa, 02=Master, 03=Amex, 04=Sorocred, 99=Outros")
    private String cardBrand;

    @Schema(description = "Número da autorização da transação (cartão)")
    private String cardAuthorization;

    @Schema(description = "Itens da venda")
    private List<SaleItemResponse> items;

    @Schema(description = "Nome do vendedor")
    private String sellerName;

    @Schema(description = "Observações da venda")
    private String notes;

    @Schema(description = "Pode emitir cupom fiscal (tenant configurado)")
    private Boolean canEmitFiscalReceipt;

    @Schema(description = "Pode emitir comprovante simples")
    private Boolean canEmitSimpleReceipt;

    @Schema(description = "Pode emitir NF-e (Nota Fiscal Eletrônica)")
    private Boolean canEmitNfe;

    @Schema(description = "NF-e indisponível por falta de CPF/CNPJ do cliente; exibe alerta para informar destinatário")
    private Boolean nfeRequiresCustomerDocument;

    @Schema(description = "Data de criação")
    private Instant createdAt;
}
