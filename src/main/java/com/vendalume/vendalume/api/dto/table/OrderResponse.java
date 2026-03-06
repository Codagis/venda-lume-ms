package com.vendalume.vendalume.api.dto.table;

import com.vendalume.vendalume.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta com dados da comanda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID id;
    private UUID tenantId;
    private UUID tableId;
    private String tableName;
    private OrderStatus status;
    private String notes;
    private Instant openedAt;
    private Instant closedAt;
    private UUID saleId;
    private List<OrderItemResponse> items;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
