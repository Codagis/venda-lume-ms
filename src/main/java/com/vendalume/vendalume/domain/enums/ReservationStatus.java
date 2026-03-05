package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

/**
 * Status da reserva no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-21
 */
@Getter
public enum ReservationStatus {

    PENDING("Pendente"),
    CONFIRMED("Confirmada"),
    SEATED("Cliente sentado"),
    CANCELLED("Cancelada"),
    NO_SHOW("Não compareceu"),
    COMPLETED("Concluída");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }
}
