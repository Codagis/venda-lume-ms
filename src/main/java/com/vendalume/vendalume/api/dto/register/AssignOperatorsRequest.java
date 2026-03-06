package com.vendalume.vendalume.api.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO de requisição para atribuir operadores ao caixa.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Schema(description = "Lista de IDs de usuários para atribuir como operadores do caixa")
public class AssignOperatorsRequest {

    @NotNull
    @Schema(description = "IDs dos usuários que podem operar este caixa")
    private List<UUID> userIds;
}
