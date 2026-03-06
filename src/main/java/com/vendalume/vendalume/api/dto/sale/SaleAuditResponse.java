package com.vendalume.vendalume.api.dto.sale;

import com.vendalume.vendalume.domain.enums.SaleAuditEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta com registro de auditoria da venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@Schema(description = "Registro de auditoria de uma venda")
public class SaleAuditResponse {

    @Schema(description = "ID do registro")
    private UUID id;

    @Schema(description = "Tipo do evento")
    private SaleAuditEventType eventType;

    @Schema(description = "Data e hora do evento")
    private Instant occurredAt;

    @Schema(description = "Nome do usuário que realizou a ação")
    private String userName;

    @Schema(description = "Descrição ou resumo da alteração")
    private String description;
}
