package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa RegisterOperator no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Entity
@Table(
    name = "register_operators",
    indexes = {
        @Index(name = "idx_register_operator_register", columnList = "register_id"),
        @Index(name = "idx_register_operator_user", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "pk_register_operator", columnNames = {"register_id", "user_id"})
    }
)
@IdClass(RegisterOperatorId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class RegisterOperator {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "register_id", nullable = false, columnDefinition = "UUID")
    private UUID registerId;

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "register_id", insertable = false, updatable = false)
    private Register register;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
