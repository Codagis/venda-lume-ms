package com.vendalume.vendalume.api.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO de opção de operador de caixa.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@Schema(description = "Operador de caixa (usuário com perfil Caixa/Operador)")
public class CashierOption {

    private UUID id;
    private String fullName;
    private String username;
    private String role;
}
