package com.commo.commo.domain.enums;

import lombok.Getter;

/**
 * Status do fluxo da venda no sistema Commo.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Getter
public enum SaleStatus {

    DRAFT("Rascunho - venda em edição"),
    OPEN("Aberta - aguardando pagamento"),
    PAID("Paga - pagamento confirmado"),
    PREPARING("Em preparação - para delivery/retirada"),
    READY("Pronta - aguardando retirada/entrega"),
    IN_DELIVERY("Em entrega"),
    DELIVERED("Entregue"),
    COMPLETED("Concluída"),
    CANCELLED("Cancelada"),
    REFUNDED("Estornada");

    private final String description;

    SaleStatus(String description) {
        this.description = description;
    }
}
