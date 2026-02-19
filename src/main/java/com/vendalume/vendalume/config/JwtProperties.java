package com.vendalume.vendalume.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração do JWT.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Component
@ConfigurationProperties(prefix = "vendalume.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenValidity = 900;
    private long refreshTokenValidity = 604800;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenValidity() {
        return accessTokenValidity;
    }

    public void setAccessTokenValidity(long accessTokenValidity) {
        this.accessTokenValidity = accessTokenValidity;
    }

    public long getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    public void setRefreshTokenValidity(long refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }
}
