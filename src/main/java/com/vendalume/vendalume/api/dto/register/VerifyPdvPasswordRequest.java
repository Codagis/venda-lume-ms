package com.vendalume.vendalume.api.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Corpo da requisição para verificar a senha de acesso do PDV.
 */
@Data
@Schema(description = "Senha do PDV para verificação")
public class VerifyPdvPasswordRequest {

    @NotBlank(message = "Senha do PDV é obrigatória")
    @Schema(description = "Senha de acesso do PDV", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
