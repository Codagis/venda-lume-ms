package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Comment;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Perfil de acesso: conjunto de permissões. Pode ser por tenant (tenant_id preenchido)
 * ou do sistema (tenant_id null). Usuários são vinculados a um perfil.
 *
 * @author VendaLume
 */
@Entity
@Table(name = "profiles", indexes = {
        @Index(name = "idx_profile_tenant", columnList = "tenant_id"),
        @Index(name = "uk_profile_tenant_name", columnList = "tenant_id, name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Profile extends BaseAuditableEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Empresa do perfil (null = perfil do sistema)")
    @Column(name = "tenant_id", columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Nome do perfil")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Comment("Descrição opcional")
    @Column(name = "description", length = 500)
    private String description;

    @BatchSize(size = 16)
    @Comment("Permissões do perfil")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "profile_permissions",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
