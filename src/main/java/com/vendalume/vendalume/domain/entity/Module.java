package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.UUID;

/**
 * Entidade que representa Module no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Entity
@Table(name = "modules", indexes = {
        @Index(name = "idx_module_active", columnList = "active"),
        @Index(name = "idx_module_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public class Module extends BaseAuditableEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Código único do módulo (ex: DASHBOARD, PRODUCTS)")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Comment("Nome para exibição no menu")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Comment("Descrição opcional")
    @Column(name = "description", length = 255)
    private String description;

    @Comment("Ícone Ant Design (ex: DashboardOutlined, ShoppingOutlined)")
    @Column(name = "icon", length = 50)
    private String icon;

    @Comment("Rota do frontend (ex: /, /products)")
    @Column(name = "route", nullable = false, length = 100)
    private String route;

    @Comment("Componente React a ser carregado (ex: Dashboard, Products)")
    @Column(name = "component", nullable = false, length = 80)
    private String component;

    @Comment("Ordem de exibição no menu")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Comment("Código da permissão mínima para visualizar a tela")
    @Column(name = "view_permission_code", nullable = false, length = 80)
    private String viewPermissionCode;

    @Comment("Módulo ativo")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
