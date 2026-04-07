package com.vendalume.vendalume.api.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Objeto de transferência (DTO) StartSessionRequest.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Data
@Schema(description = "Dados para iniciar sessão do PDV")
public class StartSessionRequest {

    @Schema(description = "Senha de acesso do PDV (obrigatória se o caixa tiver senha configurada)")
    private String pdvPassword;

    @Schema(description = "IMEI do dispositivo (obrigatório quando o caixa está vinculado a um equipamento)")
    private String deviceImei;
}
