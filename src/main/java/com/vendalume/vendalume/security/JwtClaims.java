package com.vendalume.vendalume.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Claims extraídos do token JWT.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {

    private UUID userId;
    private String username;
    private String role;
    private UUID tenantId;
    private Long tokenVersion;
    private String type;
}
