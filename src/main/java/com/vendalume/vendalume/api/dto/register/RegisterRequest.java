package com.vendalume.vendalume.api.dto.register;

import com.vendalume.vendalume.domain.enums.EquipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Dados para criar/editar ponto de venda")
public class RegisterRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Schema(description = "Nome do caixa (ex: Caixa 1)", example = "Caixa 1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Código opcional")
    private String code;

    @NotNull(message = "Tipo de equipamento é obrigatório")
    @Schema(description = "Tipo de equipamento", requiredMode = Schema.RequiredMode.REQUIRED)
    private EquipmentType equipmentType;

    @Schema(description = "Descrição ou observações")
    private String description;

    @Schema(description = "Se está ativo", example = "true")
    private Boolean active;
}
