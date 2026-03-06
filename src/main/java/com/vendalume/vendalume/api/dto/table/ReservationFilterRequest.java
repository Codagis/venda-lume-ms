package com.vendalume.vendalume.api.dto.table;

import com.vendalume.vendalume.domain.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de requisição para filtrar reservas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationFilterRequest {
    private UUID tenantId;
    private UUID tableId;
    private ReservationStatus status;
    private Instant scheduledFrom;
    private Instant scheduledTo;
    private String search;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "scheduledAt";

    @Builder.Default
    private String sortDirection = "desc";
}
