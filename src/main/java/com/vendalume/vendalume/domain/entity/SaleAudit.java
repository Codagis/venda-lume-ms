package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.SaleAuditEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Index;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa SaleAudit no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Entity
@Table(name = "sale_audit", indexes = {
        @Index(name = "idx_sale_audit_sale_id", columnList = "sale_id"),
        @Index(name = "idx_sale_audit_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_sale_audit_sale_occurred", columnList = "sale_id, occurred_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SaleAudit {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Venda auditada")
    @Column(name = "sale_id", nullable = false, columnDefinition = "UUID")
    private UUID saleId;

    @Comment("Tipo do evento: CREATED, UPDATED, CANCELLED")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private SaleAuditEventType eventType;

    @Comment("Data e hora do evento")
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Comment("Usuário que realizou a ação")
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Comment("Nome do usuário (para exibição)")
    @Column(name = "user_name", length = 255)
    private String userName;

    @Comment("Descrição ou resumo da alteração")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
