package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.auth.RegisterRequest;
import com.vendalume.vendalume.api.dto.auth.UserResponse;
import com.vendalume.vendalume.api.dto.auth.UserUpdateRequest;
import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.repository.ProfileRepository;
import com.vendalume.vendalume.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de gestão de usuários.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> listForCurrentUser() {
        var user = com.vendalume.vendalume.security.SecurityUtils.requireCurrentUser();
        if (Boolean.TRUE.equals(user.getIsRoot())) {
            return userRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
        }
        if (user.getTenantId() == null) {
            return List.of();
        }
        return userRepository.findByTenantIdOrderByUsernameAsc(user.getTenantId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getById(UUID id) {
        var current = com.vendalume.vendalume.security.SecurityUtils.requireCurrentUser();
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (!Boolean.TRUE.equals(current.getIsRoot()) && !current.getTenantId().equals(u.getTenantId())) {
            throw new IllegalArgumentException("Acesso negado");
        }
        return toResponse(u);
    }

    @Transactional
    public UserResponse create(RegisterRequest request) {
        var current = com.vendalume.vendalume.security.SecurityUtils.requireCurrentUser();
        if (!Boolean.TRUE.equals(current.getIsRoot())) {
            request.setTenantId(current.getTenantId());
        }
        return authService.register(request);
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request) {
        var current = com.vendalume.vendalume.security.SecurityUtils.requireCurrentUser();
        User u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (!Boolean.TRUE.equals(current.getIsRoot()) && !current.getTenantId().equals(u.getTenantId())) {
            throw new IllegalArgumentException("Acesso negado");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail().trim(), id)) {
                throw new IllegalArgumentException("E-mail já está cadastrado");
            }
            u.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getFullName() != null) u.setFullName(request.getFullName().trim());
        if (request.getRole() != null) u.setRole(request.getRole());
        if (Boolean.TRUE.equals(current.getIsRoot())) u.setTenantId(request.getTenantId());
        u.setProfileId(request.getProfileId());
        if (request.getActive() != null) u.setActive(request.getActive());
        if (request.getTimezone() != null) u.setTimezone(request.getTimezone());
        if (request.getLocale() != null) u.setLocale(request.getLocale());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 8) {
                throw new IllegalArgumentException("Senha deve ter no mínimo 8 caracteres");
            }
            u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        u = userRepository.save(u);
        return toResponse(u);
    }

    private UserResponse toResponse(User u) {
        String profileName = null;
        if (u.getProfileId() != null) {
            profileName = profileRepository.findById(u.getProfileId()).map(p -> p.getName()).orElse(null);
        }
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .tenantId(u.getTenantId())
                .isRoot(u.getIsRoot())
                .profileId(u.getProfileId())
                .profileName(profileName)
                .active(u.getActive())
                .timezone(u.getTimezone())
                .locale(u.getLocale())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
