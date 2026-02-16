package com.commo.commo.domain.enums;

import lombok.Getter;

/**
 * Tipo de venda ou canal de venda no sistema Commo.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Getter
public enum SaleType {

    PDV("PDV - venda no balcão"),
    DELIVERY("Delivery - entrega no endereço"),
    TAKEAWAY("Retirada - cliente retira no local"),
    ONLINE("Online - pedido via aplicativo/site"),
    WHOLESALE("Atacado - venda em grande volume"),
    CATERING("Eventos e buffet");

    private final String description;

    SaleType(String description) {
        this.description = description;
    }
}
