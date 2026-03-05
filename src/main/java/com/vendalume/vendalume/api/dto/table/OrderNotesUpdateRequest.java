package com.vendalume.vendalume.api.dto.table;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Atualização das observações da comanda")
public class OrderNotesUpdateRequest {

    @Schema(description = "Observações da comanda (para cozinha, atendimento)", maxLength = 500)
    private String notes;
}
