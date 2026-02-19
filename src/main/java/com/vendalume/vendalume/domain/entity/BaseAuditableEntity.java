package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.Instant;
import java.util.UUID;

/**
 * Classe base abstrata para entidades com suporte a auditoria e controle de concorrência otimista.
 * Fornece campos padronizados de criação, alteração e versionamento para todas as entidades
 * que necessitam rastreabilidade e prevenção de conflitos em ambientes concorrentes.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class BaseAuditableEntity {

    @Comment("Data e hora de criação do registro")
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Comment("Data e hora da última atualização do registro")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Comment("ID do usuário que criou o registro")
    @Column(name = "created_by")
    private UUID createdBy;

    @Comment("ID do usuário que realizou a última atualização")
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Comment("Versão para controle de concorrência otimista (lock otimista)")
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
