package com.vendalume.vendalume.api.dto.delivery;

import com.vendalume.vendalume.domain.enums.DeliveryPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Objeto de transferência (DTO) DeliveryUpdateRequest.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Atualizar dados da entrega")
public class DeliveryUpdateRequest {

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

    @Schema(description = "UF (2 letras)")
    private String state;

    @Schema(description = "Instruções para o entregador")
    private String instructions;

    @Schema(description = "Data prevista para entrega")
    private LocalDateTime scheduledAt;

    @Schema(description = "Prioridade")
    private DeliveryPriority priority;

    @Schema(description = "Taxa de entrega (opcional)")
    private BigDecimal deliveryFee;
}

