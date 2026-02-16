package com.commo.commo.domain.enums;

import lombok.Getter;

/**
 * Prioridade da entrega para ordenação e alocação.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Getter
public enum DeliveryPriority {

    LOW("Baixa"),
    NORMAL("Normal"),
    HIGH("Alta"),
    URGENT("Urgente");

    private final String description;

    DeliveryPriority(String description) {
        this.description = description;
    }
}
