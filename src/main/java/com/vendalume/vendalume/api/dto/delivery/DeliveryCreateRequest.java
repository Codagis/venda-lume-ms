package com.vendalume.vendalume.api.dto.delivery;

import com.vendalume.vendalume.domain.enums.DeliveryPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Criar entrega a partir de venda")
public class DeliveryCreateRequest {

    @NotNull(message = "ID da venda é obrigatório")
    @Schema(description = "ID da venda")
    private UUID saleId;

    @Schema(description = "ID da empresa. Root escolhe; não-root usa do usuário.")
    private UUID tenantId;

    @Schema(description = "Prioridade")
    private DeliveryPriority priority;

    @Schema(description = "Data prevista para entrega")
    private LocalDateTime scheduledAt;

    @Schema(description = "Instruções para o entregador")
    private String instructions;
}
