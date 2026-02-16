package com.commo.commo.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * DTO com informações do usuário autenticado.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private UUID tenantId;
    private Set<String> authorities;
}
