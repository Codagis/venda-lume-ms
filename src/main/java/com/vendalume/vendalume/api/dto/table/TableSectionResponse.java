package com.vendalume.vendalume.api.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta com dados da seção de mesas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableSectionResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private Integer displayOrder;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
