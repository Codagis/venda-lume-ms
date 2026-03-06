package com.vendalume.vendalume.api.dto.stock;

import com.vendalume.vendalume.domain.enums.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de requisição para filtrar movimentações de estoque.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockFilterRequest {

    private UUID productId;
    private StockMovementType movementType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String search;
    private Boolean lowStockOnly;
    private Boolean trackStockOnly;
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 20;
}
