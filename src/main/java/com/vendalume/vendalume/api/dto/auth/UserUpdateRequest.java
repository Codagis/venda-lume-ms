package com.vendalume.vendalume.api.dto.auth;

import com.vendalume.vendalume.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de requisição de atualização de usuário.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotNull(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 255)
    private String email;

    @NotNull(message = "Nome completo é obrigatório")
    @Size(max = 150)
    private String fullName;

    @NotNull(message = "Role é obrigatório")
    private UserRole role;

    private UUID tenantId;

    private UUID profileId;

    private String password;  // opcional: em branco = não alterar

    private Boolean active;

    @Size(max = 50)
    private String timezone;

    @Size(max = 10)
    private String locale;
}
