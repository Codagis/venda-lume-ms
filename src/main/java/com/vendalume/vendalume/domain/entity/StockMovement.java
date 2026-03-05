package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "stock_movements",
        indexes = {
                @Index(name = "idx_stock_movement_product", columnList = "product_id"),
                @Index(name = "idx_stock_movement_tenant", columnList = "tenant_id"),
                @Index(name = "idx_stock_movement_created", columnList = "created_at"),
                @Index(name = "idx_stock_movement_product_created", columnList = "product_id, created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class StockMovement extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID do produto")
    @Column(name = "product_id", nullable = false, columnDefinition = "UUID")
    private UUID productId;

    @Comment("ID do tenant")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Tipo da movimentação")
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private StockMovementType movementType;

    @Comment("Variação na quantidade: positivo = entrada, negativo = saída")
    @Column(name = "quantity_delta", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityDelta;

    @Comment("Quantidade antes da movimentação")
    @Column(name = "quantity_before", precision = 19, scale = 4)
    private BigDecimal quantityBefore;

    @Comment("Quantidade após a movimentação")
    @Column(name = "quantity_after", precision = 19, scale = 4)
    private BigDecimal quantityAfter;

    @Comment("ID da venda quando movimento é por venda")
    @Column(name = "sale_id", columnDefinition = "UUID")
    private UUID saleId;

    @Comment("Número da venda para referência")
    @Column(name = "sale_number", length = 20)
    private String saleNumber;

    @Comment("Motivo ou observação da movimentação")
    @Column(name = "notes", length = 500)
    private String notes;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
