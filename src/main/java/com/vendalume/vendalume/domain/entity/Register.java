package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.EquipmentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "registers",
    indexes = {
        @Index(name = "idx_register_tenant", columnList = "tenant_id"),
        @Index(name = "idx_register_tenant_active", columnList = "tenant_id, active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_register_tenant_name", columnNames = {"tenant_id", "name"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Register extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID do tenant")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Nome do caixa (ex: Caixa 1, Caixa 2)")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Comment("Código opcional para identificação")
    @Column(name = "code", length = 30)
    private String code;

    @Comment("Tipo de equipamento: PC, TABLET, etc.")
    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_type", nullable = false, length = 30)
    private EquipmentType equipmentType;

    @Comment("Descrição ou observações")
    @Column(name = "description", length = 500)
    private String description;

    @Comment("Se o ponto de venda está ativo")
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "register", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RegisterOperator> operators = new HashSet<>();

    public void setOperators(Set<RegisterOperator> operators) {
        this.operators = operators != null ? operators : new HashSet<>();
    }

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
