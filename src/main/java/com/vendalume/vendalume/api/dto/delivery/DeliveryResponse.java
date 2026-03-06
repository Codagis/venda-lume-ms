package com.vendalume.vendalume.api.dto.delivery;

import com.vendalume.vendalume.domain.enums.DeliveryPriority;
import com.vendalume.vendalume.domain.enums.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta com dados da entrega.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados da entrega")
public class DeliveryResponse {

    @Schema(description = "ID da entrega")
    private UUID id;

    @Schema(description = "ID da empresa")
    private UUID tenantId;

    @Schema(description = "Número da entrega")
    private String deliveryNumber;

    @Schema(description = "ID da venda")
    private UUID saleId;

    @Schema(description = "Número da venda")
    private String saleNumber;

    @Schema(description = "ID do entregador")
    private UUID deliveryPersonId;

    @Schema(description = "Nome do entregador")
    private String deliveryPersonName;

    @Schema(description = "Status da entrega")
    private DeliveryStatus status;

    @Schema(description = "Prioridade")
    private DeliveryPriority priority;

    @Schema(description = "Nome do destinatário")
    private String recipientName;

    @Schema(description = "Telefone do destinatário")
    private String recipientPhone;

    @Schema(description = "Endereço completo")
    private String address;

    @Schema(description = "Complemento")
    private String complement;

    @Schema(description = "CEP")
    private String zipCode;

    @Schema(description = "Bairro")
    private String neighborhood;

    @Schema(description = "Cidade")
    private String city;

    @Schema(description = "UF")
    private String state;

    @Schema(description = "Instruções para o entregador")
    private String instructions;

    @Schema(description = "Data prevista para entrega")
    private LocalDateTime scheduledAt;

    @Schema(description = "Data em que foi aceita")
    private LocalDateTime acceptedAt;

    @Schema(description = "Data em que foi coletada")
    private LocalDateTime pickedUpAt;

    @Schema(description = "Data em que saiu")
    private LocalDateTime departedAt;

    @Schema(description = "Data em que chegou")
    private LocalDateTime arrivedAt;

    @Schema(description = "Data em que foi entregue")
    private LocalDateTime deliveredAt;

    @Schema(description = "Taxa de entrega")
    private BigDecimal deliveryFee;

    @Schema(description = "Gorjeta")
    private BigDecimal tipAmount;

    @Schema(description = "Valor total da venda")
    private BigDecimal saleTotal;

    @Schema(description = "Motivo da falha")
    private String failureReason;

    @Schema(description = "Motivo da devolução")
    private String returnReason;

    @Schema(description = "Observações do entregador")
    private String deliveryNotes;

    @Schema(description = "Quem recebeu")
    private String receivedBy;

    @Schema(description = "URL da foto/comprovante da entrega")
    private String proofOfDeliveryUrl;

    @Schema(description = "Data de criação")
    private Instant createdAt;

    @Schema(description = "Data de atualização")
    private Instant updatedAt;
}
