package com.vendalume.vendalume.api.dto.register;

import com.vendalume.vendalume.domain.enums.EquipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta com dados do ponto de venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@Schema(description = "Ponto de venda (caixa)")
public class RegisterResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private EquipmentType equipmentType;
    private String description;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @Schema(description = "Operadores (usuários) que podem operar este caixa")
    private List<RegisterOperatorItem> operators;

    @Data
    @Builder
    public static class RegisterOperatorItem {
        private UUID userId;
        private String fullName;
        private String username;
    }
}
