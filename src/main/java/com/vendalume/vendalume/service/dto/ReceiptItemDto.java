package com.vendalume.vendalume.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Objeto de transferência (DTO) interno ao serviço: ReceiptItemDto.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Data
@Builder
public class ReceiptItemDto {

    private String descricaoProduto;
    private String codigoProduto;
    private BigDecimal quantidadeProduto;
    private BigDecimal valorUnitarioProduto;
    private BigDecimal valorTotalProduto;
}
