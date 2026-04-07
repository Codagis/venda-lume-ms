package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

/**
 * Enumeração EquipmentType.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Getter
public enum EquipmentType {

    PC("Computador"),
    NOTEBOOK("Notebook"),
    TABLET("Tablet"),
    SMARTPHONE("Celular / Smartphone"),
    OUTRO("Outro");

    private final String description;

    EquipmentType(String description) {
        this.description = description;
    }
}
