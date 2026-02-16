package com.commo.commo.domain.entity;

import com.commo.commo.domain.enums.UnitOfMeasure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade que representa um item individual de uma venda. Armazena quantidade, preços,
 * descontos e snapshot do produto para histórico imutável.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Entity
@Table(
        name = "sale_items",
        indexes = {
                @Index(name = "idx_sale_item_sale", columnList = "sale_id"),
                @Index(name = "idx_sale_item_product", columnList = "product_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "sale")
public class SaleItem {

    @Comment("Identificador único do item")
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Venda a qual o item pertence")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Comment("Produto vendido, nulo quando produto foi excluído")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Comment("Ordem de exibição do item na venda")
    @Column(name = "item_order", nullable = false)
    @Builder.Default
    private Integer itemOrder = 0;

    @Comment("Quantidade vendida")
    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Comment("Preço unitário no momento da venda")
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Comment("Unidade de medida no momento da venda")
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", length = 10)
    private UnitOfMeasure unitOfMeasure;

    @Comment("Valor do desconto aplicado neste item")
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Comment("Valor do imposto neste item")
    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Comment("Valor total do item (quantity * unitPrice - discount + tax)")
    @Column(name = "total", nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @Comment("Nome do produto no momento da venda para histórico")
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Comment("SKU do produto no momento da venda para histórico")
    @Column(name = "product_sku", length = 50)
    private String productSku;

    @Comment("Observações ou instruções específicas do item")
    @Column(name = "observations", length = 500)
    private String observations;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
