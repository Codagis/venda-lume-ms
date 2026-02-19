package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.PaymentMethod;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade que representa uma venda no sistema VendaLume. Suporta PDV, delivery, retirada e online,
 * com fluxo completo de status, pagamentos, descontos, entregas e auditoria.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Entity
@Table(
        name = "sales",
        indexes = {
                @Index(name = "idx_sale_tenant_date", columnList = "tenant_id, sale_date"),
                @Index(name = "idx_sale_tenant_status", columnList = "tenant_id, status"),
                @Index(name = "idx_sale_tenant_number", columnList = "tenant_id, sale_number"),
                @Index(name = "idx_sale_seller", columnList = "seller_id"),
                @Index(name = "idx_sale_customer", columnList = "customer_id"),
                @Index(name = "idx_sale_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sale_tenant_number", columnNames = {"tenant_id", "sale_number"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "items")
public class Sale extends BaseAuditableEntity {

    @Comment("Identificador único da venda")
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID do tenant ou organização")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Número sequencial da venda por tenant para identificação")
    @Column(name = "sale_number", nullable = false, length = 20)
    private String saleNumber;

    @Comment("Data e hora da venda")
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Comment("Status atual da venda no fluxo")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SaleStatus status = SaleStatus.DRAFT;

    @Comment("Tipo ou canal da venda")
    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = false, length = 20)
    private SaleType saleType;

    @Comment("ID do cliente quando cadastrado")
    @Column(name = "customer_id", columnDefinition = "UUID")
    private UUID customerId;

    @Comment("Nome do cliente para cupom e NF-e")
    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Comment("CPF ou CNPJ do cliente")
    @Column(name = "customer_document", length = 18)
    private String customerDocument;

    @Comment("Telefone do cliente para contato")
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Comment("E-mail do cliente para envio de cupom")
    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Comment("ID do vendedor ou operador do PDV")
    @Column(name = "seller_id", nullable = false, columnDefinition = "UUID")
    private UUID sellerId;

    @Comment("Identificador do caixa ou terminal quando aplicável")
    @Column(name = "register_id", length = 50)
    private String registerId;

    @Comment("Subtotal dos itens antes de descontos e impostos")
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Comment("Valor total de descontos aplicados na venda")
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Comment("Percentual de desconto aplicado sobre o subtotal")
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Comment("Valor total de impostos na venda")
    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Comment("Valor da taxa de entrega quando aplicável")
    @Column(name = "delivery_fee", precision = 19, scale = 4)
    private BigDecimal deliveryFee;

    @Comment("Valor total final da venda")
    @Column(name = "total", nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @Comment("Valor total recebido em pagamentos")
    @Column(name = "amount_paid", precision = 19, scale = 4)
    private BigDecimal amountPaid;

    @Comment("Valor do troco a devolver ao cliente")
    @Column(name = "change_amount", precision = 19, scale = 4)
    private BigDecimal changeAmount;

    @Comment("Forma de pagamento principal quando pagamento único")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Comment("Endereço completo para entrega")
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Comment("Complemento ou referência do endereço de entrega")
    @Column(name = "delivery_complement", length = 255)
    private String deliveryComplement;

    @Comment("CEP do endereço de entrega")
    @Column(name = "delivery_zip_code", length = 10)
    private String deliveryZipCode;

    @Comment("Bairro do endereço de entrega")
    @Column(name = "delivery_neighborhood", length = 100)
    private String deliveryNeighborhood;

    @Comment("Cidade do endereço de entrega")
    @Column(name = "delivery_city", length = 100)
    private String deliveryCity;

    @Comment("Estado/UF do endereço de entrega")
    @Column(name = "delivery_state", length = 2)
    private String deliveryState;

    @Comment("Previsão de entrega ou retirada")
    @Column(name = "expected_delivery_at")
    private LocalDateTime expectedDeliveryAt;

    @Comment("Data e hora em que a entrega foi realizada")
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Comment("Observações ou instruções do pedido")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Comment("Motivo do cancelamento quando status CANCELLED")
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Comment("Data e hora do cancelamento")
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Comment("ID do usuário que cancelou a venda")
    @Column(name = "cancelled_by", columnDefinition = "UUID")
    private UUID cancelledBy;

    @Comment("Chave da NF-e ou NFC-e quando emitida")
    @Column(name = "invoice_key", length = 50)
    private String invoiceKey;

    @Comment("Número da NF-e ou NFC-e quando emitida")
    @Column(name = "invoice_number", length = 20)
    private String invoiceNumber;

    @Builder.Default
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
