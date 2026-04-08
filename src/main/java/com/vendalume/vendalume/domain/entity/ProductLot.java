package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "product_lots",
        indexes = {
                @Index(name = "idx_product_lots_tenant_product", columnList = "tenant_id, product_id"),
                @Index(name = "idx_product_lots_expires_at", columnList = "expires_at"),
                @Index(name = "idx_product_lots_lot_code", columnList = "lot_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class ProductLot extends BaseAuditableEntity {

    @Comment("Identificador único do lote")
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID do tenant/empresa")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("ID do produto ao qual o lote pertence")
    @Column(name = "product_id", nullable = false, columnDefinition = "UUID")
    private UUID productId;

    @Comment("Código/identificador do lote")
    @Column(name = "lot_code", nullable = false, length = 60)
    private String lotCode;

    @Comment("Data de validade do lote")
    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Comment("Quantidade disponível no lote")
    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}

