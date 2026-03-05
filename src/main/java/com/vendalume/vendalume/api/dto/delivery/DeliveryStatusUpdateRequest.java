package com.vendalume.vendalume.api.dto.delivery;

import com.vendalume.vendalume.domain.enums.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Atualizar status da entrega")
public class DeliveryStatusUpdateRequest {

    @NotNull(message = "Novo status é obrigatório")
    @Schema(description = "Novo status")
    private DeliveryStatus status;

    @Schema(description = "Motivo da falha (quando status FAILED)")
    private String failureReason;

    @Schema(description = "Motivo da devolução (quando status RETURNED)")
    private String returnReason;

    @Schema(description = "Observações do entregador")
    private String deliveryNotes;

    @Schema(description = "Nome de quem recebeu (quando status DELIVERED)")
    private String receivedBy;

    @Schema(description = "URL da foto/comprovante da entrega (GCS)")
    private String proofOfDeliveryUrl;
}
