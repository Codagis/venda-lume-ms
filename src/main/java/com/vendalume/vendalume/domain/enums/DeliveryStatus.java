package com.vendalume.vendalume.domain.enums;

import lombok.Getter;

/**
 * Status do fluxo da entrega no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Getter
public enum DeliveryStatus {

    PENDING("Pendente - aguardando atribuição de entregador"),
    ASSIGNED("Atribuída - entregador designado"),
    ACCEPTED("Aceita - entregador aceitou a corrida"),
    PICKING_UP("Coletando - retirando pedido no estabelecimento"),
    PICKED_UP("Coletado - pedido retirado, em rota"),
    IN_TRANSIT("Em trânsito - a caminho do cliente"),
    ARRIVED("Chegou - entregador no local"),
    DELIVERED("Entregue - entrega concluída"),
    FAILED("Falhou - não foi possível entregar"),
    CANCELLED("Cancelada"),
    RETURNED("Devolvido - pedido retornou ao estabelecimento");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }
}
