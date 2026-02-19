package com.vendalume.vendalume.security;

import com.vendalume.vendalume.config.JwtProperties;
import com.vendalume.vendalume.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Provedor de tokens JWT para autenticação OAuth 2.0.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TENANT = "tenant_id";
    private static final String CLAIM_TOKEN_VERSION = "tvv";
    private static final String CLAIM_TYPE = "type";

    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        return generateToken(
                user.getId().toString(),
                user.getUsername(),
                user.getRole().name(),
                user.getTenantId(),
                user.getRefreshTokenVersion(),
                "access",
                jwtProperties.getAccessTokenValidity()
        );
    }

    public String generateRefreshToken(User user) {
        return generateToken(
                user.getId().toString(),
                user.getUsername(),
                user.getRole().name(),
                user.getTenantId(),
                user.getRefreshTokenVersion(),
                "refresh",
                jwtProperties.getRefreshTokenValidity()
        );
    }

    private String generateToken(String userId, String username, String role, UUID tenantId,
                                 Long tokenVersion, String type, long validitySeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validitySeconds * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TENANT, tenantId != null ? tenantId.toString() : null)
                .claim(CLAIM_TOKEN_VERSION, tokenVersion)
                .claim(CLAIM_TYPE, type)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public JwtClaims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get(CLAIM_TYPE, String.class);
            Long tokenVersion = claims.get(CLAIM_TOKEN_VERSION, Long.class);

            return JwtClaims.builder()
                    .userId(UUID.fromString(claims.getSubject()))
                    .username(claims.get(CLAIM_USERNAME, String.class))
                    .role(claims.get(CLAIM_ROLE, String.class))
                    .tenantId(claims.get(CLAIM_TENANT, String.class) != null
                            ? UUID.fromString(claims.get(CLAIM_TENANT, String.class))
                            : null)
                    .tokenVersion(tokenVersion)
                    .type(type)
                    .build();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expirado");
        } catch (JwtException e) {
            throw new JwtException("Token inválido: " + e.getMessage());
        }
    }

    public boolean isAccessToken(String token) {
        try {
            JwtClaims claims = parseToken(token);
            return "access".equals(claims.getType());
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            JwtClaims claims = parseToken(token);
            return "refresh".equals(claims.getType());
        } catch (JwtException e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret deve ter no mínimo 32 caracteres");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
