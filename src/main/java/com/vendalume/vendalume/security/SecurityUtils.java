package com.vendalume.vendalume.security;

import com.vendalume.vendalume.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Utilitários para obtenção do usuário autenticado e contexto de tenant.
 *
 * @author VendaLume
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

    /** Retorna o tenant do usuário logado, ou empty se root/sem tenant. */
    public static java.util.Optional<UUID> getTenantIdOptional() {
        return getCurrentUser().map(User::getTenantId).filter(id -> id != null);
    }

    /** Verifica se o usuário logado é root (acesso total). */
    public static boolean isCurrentUserRoot() {
        return getCurrentUser().map(u -> Boolean.TRUE.equals(u.getIsRoot())).orElse(false);
    }

    public static UUID getCurrentUserId() {
        return requireCurrentUser().getId();
    }

    /** Verifica se o contexto de segurança atual possui a autoridade informada (ex.: PERMISSION_TENANT_MANAGE). */
    public static boolean currentUserHasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (authority != null && authority.equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
