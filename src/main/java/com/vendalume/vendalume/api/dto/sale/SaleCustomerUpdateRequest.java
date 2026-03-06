package com.vendalume.vendalume.api.dto.sale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

/**
 * DTO de requisição para atualizar cliente da venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Schema(description = "Atualização do cliente da venda (nome e documento). Alteração auditada.")
public class SaleCustomerUpdateRequest {

    @Schema(description = "ID do cliente cadastrado (opcional)")
    private UUID customerId;

    @Schema(description = "Nome do cliente")
    private String customerName;

    @Schema(description = "CPF ou CNPJ do cliente")
    private String customerDocument;
}
