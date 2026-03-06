package com.vendalume.vendalume.api.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta com dados de uma sessão de PDV.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-03-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sessão de PDV (abertura/fechamento do caixa)")
public class RegisterSessionResponse {

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
    @Schema(description = "Quantidade de vendas realizadas na sessão")
    private long salesCount;
    @Schema(description = "Valor total das vendas na sessão")
    private java.math.BigDecimal totalSales;
}
