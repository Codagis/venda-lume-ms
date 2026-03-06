package com.vendalume.vendalume.api.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * DTO de requisição para criar/editar perfil.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {

    private UUID tenantId;

    @NotBlank(message = "Nome do perfil é obrigatório")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private Set<UUID> permissionIds;
}
