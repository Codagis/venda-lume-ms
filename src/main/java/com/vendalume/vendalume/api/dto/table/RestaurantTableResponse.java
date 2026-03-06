package com.vendalume.vendalume.api.dto.table;

import com.vendalume.vendalume.domain.enums.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta com dados da mesa.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableResponse {
    private UUID id;
    private UUID tenantId;
    private UUID sectionId;
    private String sectionName;
    private String name;
    private Integer capacity;
    private TableStatus status;
    private Boolean active;
    private Integer positionX;
    private Integer positionY;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
