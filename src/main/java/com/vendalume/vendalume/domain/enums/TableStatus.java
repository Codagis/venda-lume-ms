package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

/**
 * Status da mesa no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-21
 */
@Getter
public enum TableStatus {

    AVAILABLE("Disponível"),
    OCCUPIED("Ocupada"),
    RESERVED("Reservada"),
    MAINTENANCE("Em manutenção");

    private final String description;

    TableStatus(String description) {
        this.description = description;
    }
}
