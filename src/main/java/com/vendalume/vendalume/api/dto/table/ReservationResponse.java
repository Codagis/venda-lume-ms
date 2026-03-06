package com.vendalume.vendalume.api.dto.table;

import com.vendalume.vendalume.domain.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta com dados da reserva.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private UUID id;
    private UUID tenantId;
    private UUID tableId;
    private String tableName;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Instant scheduledAt;
    private Integer numberOfGuests;
    private ReservationStatus status;
    private String notes;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
