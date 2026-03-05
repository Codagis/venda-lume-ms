package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

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
