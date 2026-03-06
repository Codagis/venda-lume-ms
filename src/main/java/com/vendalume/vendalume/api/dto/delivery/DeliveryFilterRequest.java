package com.vendalume.vendalume.api.dto.delivery;

import com.vendalume.vendalume.domain.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de requisição para filtrar entregas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFilterRequest {

    private UUID tenantId;
    private DeliveryStatus status;
    private UUID deliveryPersonId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String search;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}
