package com.vendalume.vendalume.api.dto.stock;

import com.vendalume.vendalume.domain.enums.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private StockMovementType movementType;
    private String movementTypeLabel;
    private BigDecimal quantityDelta;
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    private UUID saleId;
    private String saleNumber;
    private String notes;
    private String createdByName;
    private Instant createdAt;
}
