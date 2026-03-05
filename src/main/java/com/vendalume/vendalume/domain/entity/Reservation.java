package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa uma reserva de mesa.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-21
 */
@Entity
@Table(name = "reservation", indexes = {
    @Index(name = "idx_reservation_tenant", columnList = "tenant_id"),
    @Index(name = "idx_reservation_table", columnList = "table_id"),
    @Index(name = "idx_reservation_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_reservation_tenant_scheduled", columnList = "tenant_id, scheduled_at")
})
@Comment("Reservas de mesas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Reservation extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Tenant (empresa)")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Mesa reservada")
    @Column(name = "table_id", nullable = false, columnDefinition = "UUID")
    private UUID tableId;

    @Comment("Nome do cliente")
    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Comment("Telefone do cliente")
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Comment("E-mail do cliente")
    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Comment("Data/hora agendada")
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Comment("Número de pessoas")
    @Column(name = "number_of_guests", nullable = false)
    private Integer numberOfGuests = 1;

    @Comment("Status da reserva")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Comment("Observações")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
