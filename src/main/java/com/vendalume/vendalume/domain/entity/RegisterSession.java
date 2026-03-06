package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.Instant;
import java.util.UUID;

/**
 * Sessão de PDV: registro de abertura/fechamento do caixa por um operador para auditoria e histórico.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-03-05
 */
@Entity
@Table(
    name = "register_session",
    indexes = {
        @Index(name = "idx_register_session_register", columnList = "register_id"),
        @Index(name = "idx_register_session_user", columnList = "user_id"),
        @Index(name = "idx_register_session_tenant", columnList = "tenant_id"),
        @Index(name = "idx_register_session_opened", columnList = "opened_at"),
        @Index(name = "idx_register_session_register_opened", columnList = "register_id, opened_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RegisterSession {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Caixa (PDV) da sessão")
    @Column(name = "register_id", nullable = false, columnDefinition = "UUID")
    private UUID registerId;

    @Comment("Operador que abriu a sessão")
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    @Comment("Tenant da sessão")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Data/hora de abertura do PDV")
    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Comment("Data/hora de fechamento (null se ainda aberta)")
    @Column(name = "closed_at")
    private Instant closedAt;

    @Comment("Data de criação do registro")
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }
}
