package com.commo.commo.domain.enums;

import lombok.Getter;

/**
 * Formas de pagamento aceitas no sistema Commo.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Getter
public enum PaymentMethod {

    CASH("Dinheiro"),
    CREDIT_CARD("Cartão de crédito"),
    DEBIT_CARD("Cartão de débito"),
    PIX("PIX"),
    BANK_TRANSFER("Transferência bancária"),
    MEAL_VOUCHER("Vale refeição"),
    FOOD_VOUCHER("Vale alimentação"),
    CHECK("Cheque"),
    CREDIT("Crédito/fiado"),
    OTHER("Outro");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}
