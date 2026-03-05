package com.vendalume.vendalume.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SimpleReceiptItemDto {

    private String descricaoProduto;
    private String observacaoItem;
    private BigDecimal quantidadeProduto;
    private BigDecimal valorUnitarioProduto;
    private BigDecimal valorTotalProduto;
}
