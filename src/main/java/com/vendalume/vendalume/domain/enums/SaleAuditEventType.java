package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

/**
 * Enumeração SaleAuditEventType.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Getter
public enum SaleAuditEventType {
    CREATED("Criada"),
    UPDATED("Editada"),
    CANCELLED("Cancelada");

    private final String description;

    SaleAuditEventType(String description) {
        this.description = description;
    }
}
