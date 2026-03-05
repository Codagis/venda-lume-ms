package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

/**
 * Status de contas a pagar e contas a receber.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-18
 */
@Getter
public enum AccountStatus {

    PENDING("Pendente"),
    PARTIAL("Parcialmente pago"),
    PAID("Pago"),
    OVERDUE("Em atraso"),
    CANCELLED("Cancelado");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }
}
