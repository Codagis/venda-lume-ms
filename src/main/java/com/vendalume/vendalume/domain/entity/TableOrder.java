package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Comment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade que representa TableOrder no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Entity
@Table(name = "table_order", indexes = {
    @Index(name = "idx_table_order_tenant", columnList = "tenant_id"),
    @Index(name = "idx_table_order_table", columnList = "table_id"),
    @Index(name = "idx_table_order_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_table_order_sale", columnList = "sale_id"),
    @Index(name = "idx_table_order_opened_at", columnList = "opened_at"),
    @Index(name = "idx_table_order_created_at", columnList = "created_at")
})
@Comment("Comandas (pedidos de mesa)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "items")

public class TableOrder extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Tenant (empresa)")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Mesa da comanda")
    @Column(name = "table_id", nullable = false, columnDefinition = "UUID")
    private UUID tableId;

    @Comment("Status da comanda (OPEN/CLOSED)")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.OPEN;

    @Comment("Data/hora de abertura")
    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Comment("Data/hora de fechamento")
    @Column(name = "closed_at")
    private Instant closedAt;

    @Comment("Venda gerada ao fechar a comanda")
    @Column(name = "sale_id", columnDefinition = "UUID")
    private UUID saleId;

    @Comment("Observações da comanda (para cozinha, atendimento)")
    @Column(name = "notes", length = 500)
    private String notes;

    @BatchSize(size = 16)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TableOrderItem> items = new ArrayList<>();

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
