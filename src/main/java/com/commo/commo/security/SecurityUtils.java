package com.commo.commo.security;

import com.commo.commo.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Utilitários para obtenção do usuário autenticado e contexto de tenant.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public static User requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Usuário não autenticado"));
    }

    public static UUID requireTenantId() {
        User user = requireCurrentUser();
        if (user.getTenantId() == null) {
            throw new IllegalStateException("Operação requer contexto de tenant");
        }
        return user.getTenantId();
    }

    public static UUID getCurrentUserId() {
        return requireCurrentUser().getId();
    }
}
