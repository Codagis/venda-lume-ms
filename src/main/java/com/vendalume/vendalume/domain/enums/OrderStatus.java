package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

/**
 * Status da comanda (pedido de mesa) no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-21
 */
@Getter
public enum OrderStatus {

    OPEN("Aberta"),
    CLOSED("Fechada");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
