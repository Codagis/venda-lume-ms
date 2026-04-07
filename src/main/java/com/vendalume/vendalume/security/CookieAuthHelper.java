package com.vendalume.vendalume.security;

import com.vendalume.vendalume.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Componente de segurança CookieAuthHelper.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Component
@RequiredArgsConstructor
public class CookieAuthHelper {

    private static final int MAX_AGE_DELETE = 0;
    private static final String ROOT_PATH = "/";
    private static final String EXPIRES_EPOCH = "Thu, 01 Jan 1970 00:00:00 GMT";

    private final JwtProperties jwtProperties;

    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        addAuthCookie(response,
                jwtProperties.getAccessTokenCookieName(),
                accessToken,
                (int) jwtProperties.getAccessTokenValidity());
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        addAuthCookie(response,
                jwtProperties.getRefreshTokenCookieName(),
                refreshToken,
                (int) jwtProperties.getRefreshTokenValidity());
    }

    private void addAuthCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        String header = buildSetCookieHeader(name, value, maxAgeSeconds);
        response.addHeader("Set-Cookie", header);
    }

    private String buildSetCookieHeader(String name, String value, int maxAgeSeconds) {
        StringBuilder sb = new StringBuilder(name).append('=').append(value)
                .append("; Path=").append(jwtProperties.getCookiePath())
                .append("; Max-Age=").append(maxAgeSeconds)
                .append(maxAgeSeconds == 0 ? "; Expires=" + EXPIRES_EPOCH : "")
                .append("; HttpOnly");
        if (jwtProperties.isCookieSecure()) sb.append("; Secure");
        String sameSite = jwtProperties.getCookieSameSite();
        if (sameSite != null && !sameSite.isEmpty()) {
            sb.append("; SameSite=").append(sameSite);
        }
        return sb.toString();
    }

    public void clearAuthCookies(HttpServletResponse response) {
        clearCookie(response, jwtProperties.getAccessTokenCookieName());
        clearCookie(response, jwtProperties.getRefreshTokenCookieName());

        /**
         * Garantia de remoção:
         * em alguns ambientes o cookie pode ter sido gravado com Path="/"
         * (ex.: reverse proxy/context-path diferente). Para evitar "logout que não desloga",
         * também enviamos Set-Cookie de deleção no root path.
         */
        clearCookieWithPath(response, jwtProperties.getAccessTokenCookieName(), ROOT_PATH);
        clearCookieWithPath(response, jwtProperties.getRefreshTokenCookieName(), ROOT_PATH);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        String header = buildSetCookieHeader(name, "", MAX_AGE_DELETE);
        response.addHeader("Set-Cookie", header);
    }

    private void clearCookieWithPath(HttpServletResponse response, String name, String path) {
        String header = buildSetCookieHeaderWithPath(name, "", MAX_AGE_DELETE, path);
        response.addHeader("Set-Cookie", header);
    }

    private String buildSetCookieHeaderWithPath(String name, String value, int maxAgeSeconds, String path) {
        StringBuilder sb = new StringBuilder(name).append('=').append(value)
                .append("; Path=").append(path)
                .append("; Max-Age=").append(maxAgeSeconds)
                .append(maxAgeSeconds == 0 ? "; Expires=" + EXPIRES_EPOCH : "")
                .append("; HttpOnly");
        if (jwtProperties.isCookieSecure()) sb.append("; Secure");
        String sameSite = jwtProperties.getCookieSameSite();
        if (sameSite != null && !sameSite.isEmpty()) {
            sb.append("; SameSite=").append(sameSite);
        }
        return sb.toString();
    }

    public String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    public String getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, jwtProperties.getAccessTokenCookieName());
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, jwtProperties.getRefreshTokenCookieName());
    }
}
