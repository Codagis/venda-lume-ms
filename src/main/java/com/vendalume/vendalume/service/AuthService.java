package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.auth.LoginRequest;
import com.vendalume.vendalume.api.dto.auth.LoginResponse;
import com.vendalume.vendalume.api.dto.auth.RefreshRequest;
import com.vendalume.vendalume.api.dto.auth.RegisterRequest;
import com.vendalume.vendalume.api.dto.auth.UserInfo;
import com.vendalume.vendalume.api.dto.auth.UserResponse;
import com.vendalume.vendalume.config.JwtProperties;
import com.vendalume.vendalume.domain.entity.Permission;
import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.repository.ProfileRepository;
import com.vendalume.vendalume.repository.UserRepository;
import com.vendalume.vendalume.security.CookieAuthHelper;
import com.vendalume.vendalume.security.JwtClaims;
import com.vendalume.vendalume.security.JwtTokenProvider;
import com.vendalume.vendalume.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Serviço de autenticação com JWT e cadastro de usuários.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final CookieAuthHelper cookieAuthHelper;

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        User user = authenticateUser(request);
        LoginResponse tokenResponse = buildTokenResponse(user);
        cookieAuthHelper.addAccessTokenCookie(response, tokenResponse.getAccessToken());
        cookieAuthHelper.addRefreshTokenCookie(response, tokenResponse.getRefreshToken());
        return buildLoginResponseWithoutTokens(tokenResponse);
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        String refreshTokenValue = request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()
                ? request.getRefreshToken()
                : cookieAuthHelper.getRefreshTokenFromCookie(httpRequest);

        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new BadCredentialsException("Refresh token ausente. Faça login novamente.");
        }

        JwtClaims claims = jwtTokenProvider.parseToken(refreshTokenValue);

        if (!"refresh".equals(claims.getType())) {
            throw new BadCredentialsException("Token inválido");
        }

        User user = userRepository.findById(claims.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Usuário inativo");
        }

        if (!claims.getTokenVersion().equals(user.getRefreshTokenVersion())) {
            throw new BadCredentialsException("Token revogado. Faça login novamente.");
        }

        LoginResponse tokenResponse = buildTokenResponse(user);
        cookieAuthHelper.addAccessTokenCookie(response, tokenResponse.getAccessToken());
        cookieAuthHelper.addRefreshTokenCookie(response, tokenResponse.getRefreshToken());
        return buildLoginResponseWithoutTokens(tokenResponse);
    }

    public void logout(HttpServletResponse response) {
        cookieAuthHelper.clearAuthCookies(response);
    }

    @Transactional(readOnly = true)
    public UserInfo getCurrentUserInfo() {
        User user = SecurityUtils.requireCurrentUser();
        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .tenantId(user.getTenantId())
                .isRoot(user.getIsRoot())
                .profileId(user.getProfileId())
                .authorities(resolveAuthorities(user))
                .build();
    }

    private Set<String> resolveAuthorities(User user) {
        Set<String> authorities = new HashSet<>();
        user.getAuthorities().forEach(a -> authorities.add(a.getAuthority()));
        if (user.getProfileId() != null) {
            profileRepository.findByIdWithPermissions(user.getProfileId()).ifPresent(profile -> {
                if (profile.getPermissions() != null) {
                    for (Permission p : profile.getPermissions()) {
                        if (p != null && p.getCode() != null && !p.getCode().isBlank()) {
                            authorities.add("PERMISSION_" + p.getCode().trim());
                        }
                    }
                }
            });
        }
        return authorities;
    }

    private User authenticateUser(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Usuário inativo");
        }

        if (!user.isAccountNonLocked()) {
            throw new LockedException("Conta bloqueada temporariamente. Tente novamente mais tarde.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            userRepository.incrementFailedLoginAttempts(user.getId());
            if (user.getFailedLoginAttempts() + 1 >= MAX_FAILED_ATTEMPTS) {
                userRepository.lockAccount(user.getId(), Instant.now().plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES));
                throw new LockedException("Conta bloqueada após várias tentativas. Tente novamente em 15 minutos.");
            }
            throw new BadCredentialsException("Credenciais inválidas");
        }

        userRepository.recordSuccessfulLogin(user.getId(), Instant.now());
        user.setLastLoginAt(Instant.now());
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        return user;
    }

    private LoginResponse buildLoginResponseWithoutTokens(LoginResponse full) {
        return LoginResponse.builder()
                .accessToken(null)
                .refreshToken(null)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenValidity())
                .refreshExpiresIn(jwtProperties.getRefreshTokenValidity())
                .user(full.getUser())
                .build();
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("Username já está em uso");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("E-mail já está cadastrado");
        }

        User user = User.builder()
                .username(normalizeUsername(request.getUsername()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(normalizeEmail(request.getEmail()))
                .fullName(request.getFullName())
                .cpf(request.getCpf())
                .phone(request.getPhone())
                .role(request.getRole())
                .tenantId(request.getTenantId())
                .profileId(request.getProfileId())
                .timezone(Optional.ofNullable(request.getTimezone()).orElse("America/Sao_Paulo"))
                .locale(Optional.ofNullable(request.getLocale()).orElse("pt_BR"))
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        user = userRepository.save(user);

        return toUserResponse(user);
    }

    private LoginResponse buildTokenResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .tenantId(user.getTenantId())
                .isRoot(user.getIsRoot())
                .profileId(user.getProfileId())
                .authorities(resolveAuthorities(user))
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenValidity())
                .refreshExpiresIn(jwtProperties.getRefreshTokenValidity())
                .user(userInfo)
                .build();
    }

    private String normalizeUsername(String username) {
        return username != null ? username.toLowerCase().trim() : null;
    }

    private String normalizeEmail(String email) {
        return email != null ? email.toLowerCase().trim() : null;
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .tenantId(user.getTenantId())
                .isRoot(user.getIsRoot())
                .profileId(user.getProfileId())
                .active(user.getActive())
                .timezone(user.getTimezone())
                .locale(user.getLocale())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
