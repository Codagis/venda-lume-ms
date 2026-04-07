package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

/**
 * Enumeração StockMovementType.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Getter
public enum StockMovementType {

    SALE("Venda", "Baixa por venda"),
    MANUAL_ENTRY("Entrada manual", "Entrada de estoque manual"),
    MANUAL_EXIT("Saída manual", "Saída de estoque manual"),
    ADJUSTMENT("Ajuste", "Ajuste de inventário");

    private final String label;
    private final String description;

    StockMovementType(String label, String description) {
        this.label = label;
        this.description = description;
    }
}
