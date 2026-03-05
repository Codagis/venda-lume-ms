package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade que representa um item de uma comanda (pedido de mesa).
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-21
 */
@Entity
@Table(name = "table_order_item", indexes = {
    @Index(name = "idx_table_order_item_order", columnList = "order_id"),
    @Index(name = "idx_table_order_item_product", columnList = "product_id")
})
@Comment("Itens de uma comanda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "order")
public class TableOrderItem {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Comanda a qual o item pertence")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private TableOrder order;

    @Comment("Produto vendido")
    @Column(name = "product_id", nullable = false, columnDefinition = "UUID")
    private UUID productId;

    @Comment("Quantidade")
    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Comment("Preço unitário no momento da inclusão")
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Comment("Nome do produto no momento da inclusão")
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Comment("SKU do produto no momento da inclusão")
    @Column(name = "product_sku", length = 50)
    private String productSku;

    @Comment("Ordem de exibição do item na comanda")
    @Column(name = "item_order", nullable = false)
    @Builder.Default
    private Integer itemOrder = 0;

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
