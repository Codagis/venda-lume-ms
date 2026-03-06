package com.vendalume.vendalume.api.dto.register;

import com.vendalume.vendalume.api.dto.sale.SaleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta com detalhe da sessão de PDV e vendas realizadas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-03-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalhe da sessão de PDV com lista de vendas")
public class RegisterSessionDetailResponse {

    private UUID id;
    private UUID registerId;
    private String registerName;
    private UUID userId;
    private String userFullName;
    private String username;
    private UUID tenantId;
    private Instant openedAt;
    private Instant closedAt;
    private Instant createdAt;
    private List<SaleResponse> sales;
    private long salesCount;
    private BigDecimal totalSales;
}
