package com.commo.commo.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta com dados do usuário.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private UUID tenantId;
    private Boolean active;
    private String timezone;
    private String locale;
    private Instant createdAt;
}
