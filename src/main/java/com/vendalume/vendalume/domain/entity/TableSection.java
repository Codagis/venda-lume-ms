package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.UUID;

/**
 * Entidade que representa uma seção de mesas no restaurante.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-21
 */
@Entity
@Table(name = "table_section", indexes = {
    @Index(name = "idx_table_section_tenant", columnList = "tenant_id"),
    @Index(name = "idx_table_section_tenant_order", columnList = "tenant_id, display_order")
})
@Comment("Seções de mesas (áreas do restaurante)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TableSection extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Tenant (empresa)")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Nome da seção")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Comment("Descrição da seção")
    @Column(name = "description", length = 500)
    private String description;

    @Comment("Ordem de exibição")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
