package com.vendalume.vendalume.api.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Operador de caixa (usuário com perfil Caixa/Operador)")
public class CashierOption {

    private UUID id;
    private String fullName;
    private String username;
    private String role;
}
