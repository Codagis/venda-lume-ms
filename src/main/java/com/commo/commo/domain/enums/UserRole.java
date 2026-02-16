package com.commo.commo.domain.enums;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enumeração dos papéis de usuário do SaaS Commo. Define as permissões e o nível de acesso
 * no sistema, mapeando cada role para autoridades do Spring Security.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 * @see org.springframework.security.core.GrantedAuthority
 */
@Getter
public enum UserRole {

    SUPER_ADMIN("ROLE_SUPER_ADMIN", Set.of("FULL_SYSTEM_ACCESS")),

    TENANT_ADMIN("ROLE_TENANT_ADMIN", Set.of("MANAGE_USERS", "MANAGE_SETTINGS", "VIEW_REPORTS", "MANAGE_PRODUCTS")),

    MANAGER("ROLE_MANAGER", Set.of("MANAGE_OPERATIONS", "VIEW_REPORTS", "MANAGE_PRODUCTS", "MANAGE_ORDERS")),

    OPERATOR("ROLE_OPERATOR", Set.of("POS_ACCESS", "MANAGE_ORDERS", "VIEW_PRODUCTS")),

    CASHIER("ROLE_CASHIER", Set.of("POS_ACCESS", "MANAGE_ORDERS", "VIEW_PRODUCTS", "CLOSE_REGISTER")),

    DELIVERY("ROLE_DELIVERY", Set.of("VIEW_ORDERS", "MANAGE_DELIVERIES", "VIEW_PRODUCTS")),

    CUSTOMER_SUPPORT("ROLE_CUSTOMER_SUPPORT", Set.of("VIEW_ORDERS", "VIEW_CUSTOMERS", "VIEW_PRODUCTS")),

    VIEWER("ROLE_VIEWER", Set.of("VIEW_REPORTS", "VIEW_PRODUCTS", "VIEW_ORDERS"));

    private final String authority;
    private final Set<String> permissions;

    UserRole(String authority, Set<String> permissions) {
        this.authority = authority;
        this.permissions = permissions;
    }

    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = permissions.stream()
                .map(p -> new SimpleGrantedAuthority("PERMISSION_" + p))
                .collect(Collectors.toSet());
        authorities.add(new SimpleGrantedAuthority(authority));
        return authorities;
    }

    public static UserRole fromAuthority(String authority) {
        return Arrays.stream(values())
                .filter(role -> role.authority.equals(authority))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role inválido: " + authority));
    }
}
