package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.TableStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.UUID;

/**
 * Entidade que representa uma mesa do restaurante.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-21
 */
@Entity
@Table(name = "restaurant_table", indexes = {
    @Index(name = "idx_restaurant_table_tenant", columnList = "tenant_id"),
    @Index(name = "idx_restaurant_table_section", columnList = "section_id"),
    @Index(name = "idx_restaurant_table_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_restaurant_table_tenant_active", columnList = "tenant_id, active")
})
@Comment("Mesas do restaurante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RestaurantTable extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Tenant (empresa)")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Seção da mesa")
    @Column(name = "section_id", nullable = false, columnDefinition = "UUID")
    private UUID sectionId;

    @Comment("Nome/identificação da mesa")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Comment("Capacidade de pessoas")
    @Column(name = "capacity", nullable = false)
    private Integer capacity = 2;

    @Comment("Status da mesa")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TableStatus status = TableStatus.AVAILABLE;

    @Comment("Mesa ativa")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Comment("Posição X no layout do mapa de mesas")
    @Column(name = "position_x")
    private Integer positionX;

    @Comment("Posição Y no layout do mapa de mesas")
    @Column(name = "position_y")
    private Integer positionY;

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
