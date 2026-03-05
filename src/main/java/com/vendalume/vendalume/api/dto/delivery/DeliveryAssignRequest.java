package com.vendalume.vendalume.api.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Atribuir entregador à entrega")
public class DeliveryAssignRequest {

    @NotNull(message = "ID do entregador é obrigatório")
    @Schema(description = "ID do usuário entregador")
    private UUID deliveryPersonId;
}
