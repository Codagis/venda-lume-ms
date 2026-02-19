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
 * Empresa cliente do SaaS (tenant). Cada tenant tem dados isolados.
 * Codagis é a dona do SaaS e vende para outras empresas.
 *
 * @author VendaLume
 */
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_active", columnList = "active"),
        @Index(name = "idx_tenant_document", columnList = "document")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Tenant extends BaseAuditableEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Razão social")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Comment("Nome fantasia")
    @Column(name = "trade_name", length = 255)
    private String tradeName;

    @Comment("CNPJ ou documento da empresa")
    @Column(name = "document", length = 20)
    private String document;

    @Comment("E-mail de contato")
    @Column(name = "email", length = 255)
    private String email;

    @Comment("Telefone de contato")
    @Column(name = "phone", length = 20)
    private String phone;

    @Comment("Empresa ativa no sistema")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
