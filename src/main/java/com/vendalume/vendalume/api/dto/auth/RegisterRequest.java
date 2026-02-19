package com.vendalume.vendalume.api.dto.auth;

import com.vendalume.vendalume.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de requisição de cadastro de usuário.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String password;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(max = 150)
    private String fullName;

    @Size(max = 14)
    private String cpf;

    @Size(max = 20)
    private String phone;

    @NotNull(message = "Role é obrigatório")
    private UserRole role;

    private UUID tenantId;

    private UUID profileId;

    @Size(max = 50)
    private String timezone;

    @Size(max = 10)
    private String locale;
}
