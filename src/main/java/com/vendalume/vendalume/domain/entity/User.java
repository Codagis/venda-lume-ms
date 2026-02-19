package com.vendalume.vendalume.domain.entity;

import com.vendalume.vendalume.domain.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Entidade que representa um usuário do sistema VendaLume. Implementa {@code UserDetails} para
 * integração com Spring Security. Suporta autenticação por username/senha, multi-tenancy,
 * controle de tentativas de login, bloqueio temporário, 2FA e auditoria completa.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 * @see org.springframework.security.core.userdetails.UserDetails
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_cpf", columnList = "cpf"),
                @Index(name = "idx_user_tenant_active", columnList = "tenant_id, active"),
                @Index(name = "idx_user_tenant_username", columnList = "tenant_id, username"),
                @Index(name = "idx_user_active", columnList = "active"),
                @Index(name = "idx_user_created_at", columnList = "created_at"),
                @Index(name = "idx_user_last_login", columnList = "last_login_at"),
                @Index(name = "idx_user_locked_until", columnList = "locked_until")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"passwordHash", "twoFactorSecret"})
public class User extends BaseAuditableEntity implements UserDetails {

    @Comment("Identificador único do usuário")
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Nome de usuário para login, único no sistema e armazenado em minúsculas")
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Comment("Senha hasheada com BCrypt, nunca armazenar em texto plano")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Comment("Endereço de e-mail do usuário, único no sistema")
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Comment("Nome completo do usuário")
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Comment("CPF do usuário, documento brasileiro (apenas dígitos ou formatado)")
    @Column(name = "cpf", length = 14)
    private String cpf;

    @Comment("Telefone com DDD para contato")
    @Column(name = "phone", length = 20)
    private String phone;

    @Comment("URL da foto de perfil ou avatar do usuário")
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Comment("Indica se o usuário está ativo e pode realizar login")
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Comment("Indica se o e-mail foi verificado")
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Comment("Indica se o telefone foi verificado")
    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Comment("Papel ou perfil de acesso do usuário no sistema")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    @Comment("ID do tenant ou organização no modelo multi-tenancy. Nulo para SUPER_ADMIN/root")
    @Column(name = "tenant_id", columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Usuário root (Codagis): acesso total a todas as empresas e funcionalidades")
    @Column(name = "is_root", nullable = false)
    @Builder.Default
    private Boolean isRoot = false;

    @Comment("Perfil de acesso (permissões granulares). Se null, usa role")
    @Column(name = "profile_id", columnDefinition = "UUID")
    private UUID profileId;

    @Comment("Timezone preferido do usuário (ex: America/Sao_Paulo)")
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "America/Sao_Paulo";

    @Comment("Locale preferido para idioma e formatação (ex: pt_BR)")
    @Column(name = "locale", length = 10)
    @Builder.Default
    private String locale = "pt_BR";

    @Comment("Data e hora do último login realizado")
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Comment("Contador de tentativas de login com senha incorreta")
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Comment("Data e hora até a qual a conta permanece bloqueada após muitas falhas de login")
    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Comment("Indica se a autenticação em dois fatores (2FA) está habilitada")
    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Comment("Secret criptografado para geração de códigos TOTP no 2FA")
    @Column(name = "two_factor_secret", length = 255)
    private String twoFactorSecret;

    @Comment("Versão usada para invalidar tokens de refresh e forçar novo login")
    @Column(name = "refresh_token_version", nullable = false)
    @Builder.Default
    private Long refreshTokenVersion = 0L;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public void setUsername(String username) {
        this.username = username != null ? username.toLowerCase().trim() : null;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role != null ? role.getAuthorities() : Collections.emptySet();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || Instant.now().isAfter(lockedUntil);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(active);
    }
}
