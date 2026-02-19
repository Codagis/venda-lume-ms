package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.Instant;
import java.util.UUID;

/**
 * Permissão granular do sistema. Agrupadas por módulo (PRODUCTS, SALES, etc).
 *
 * @author VendaLume
 */
@Entity
@Table(name = "permissions",
        indexes = @Index(name = "idx_permission_module", columnList = "module"),
        uniqueConstraints = @UniqueConstraint(name = "uk_permission_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Permission {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Código único da permissão (ex: PRODUCT_VIEW)")
    @Column(name = "code", nullable = false, length = 80)
    private String code;

    @Comment("Nome para exibição")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Comment("Descrição opcional")
    @Column(name = "description", length = 500)
    private String description;

    @Comment("Módulo do sistema (PRODUCTS, SALES, USERS, etc)")
    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @jakarta.persistence.Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @jakarta.persistence.PrePersist
    protected void onPrePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) this.createdAt = now;
        this.updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
