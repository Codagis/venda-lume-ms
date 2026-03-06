package com.vendalume.vendalume.api.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Corpo da requisição para iniciar sessão do PDV.
 * Se o PDV tiver senha de acesso configurada, pdvPassword é obrigatório.
 */
@Data
@Schema(description = "Dados para iniciar sessão do PDV")
public class StartSessionRequest {

    @Schema(description = "Senha de acesso do PDV (obrigatória se o caixa tiver senha configurada)")
    private String pdvPassword;

    @Schema(description = "IMEI do dispositivo (obrigatório quando o caixa está vinculado a um equipamento)")
    private String deviceImei;
}
