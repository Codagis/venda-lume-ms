package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.CostCategoryKind;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.UUID;

@Entity
@Table(name = "cost_account_category", indexes = {
        @Index(name = "idx_ccc_tenant_kind", columnList = "tenant_id, kind"),
        @Index(name = "idx_ccc_tenant_kind_active", columnList = "tenant_id, kind, active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CostAccountCategory extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Tenant")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20)
    private CostCategoryKind kind;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
