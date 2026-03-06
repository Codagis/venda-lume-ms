package com.vendalume.vendalume.api.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de resposta com item da comanda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private UUID id;
    private UUID orderId;
    private UUID productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String productName;
    private String productSku;
    private Integer itemOrder;
    private BigDecimal total;
}
