package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

/**
 * Tipo de equipamento do ponto de venda (caixa).
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
