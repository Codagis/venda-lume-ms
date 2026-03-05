package com.vendalume.vendalume.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Item do cupom para o template Thymeleaf.
 * Variáveis alinhadas ao template de cupom fiscal.
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
