package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.permission.PermissionResponse;
import com.vendalume.vendalume.api.dto.profile.ProfileRequest;
import com.vendalume.vendalume.api.dto.profile.ProfileResponse;
import com.vendalume.vendalume.domain.entity.Permission;
import com.vendalume.vendalume.domain.entity.Profile;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.repository.PermissionRepository;
import com.vendalume.vendalume.repository.ProfileRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PermissionRepository permissionRepository;

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
        // Se não for root, ignora requestTenantId e usa o do usuário
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> listByTenant(UUID tenantId) {
        if (SecurityUtils.isCurrentUserRoot() && tenantId == null) {
            // Root sem filtro: retorna TODOS os perfis (sistema + empresas)
            return profileRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
        }
        UUID effectiveTenantId = resolveTenantId(tenantId);
        List<Profile> list = effectiveTenantId == null
                ? profileRepository.findByTenantIdIsNullOrderByNameAsc()
                : profileRepository.findByTenantIdOrderByNameAsc(effectiveTenantId);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProfileResponse findById(UUID id) {
        Profile profile = profileRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil", id));
        if (!SecurityUtils.isCurrentUserRoot() && profile.getTenantId() != null
                && !profile.getTenantId().equals(SecurityUtils.requireTenantId())) {
            throw new ResourceNotFoundException("Perfil", id);
        }
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse create(ProfileRequest request) {
        UUID tenantId = resolveTenantId(request.getTenantId());
        boolean nameExists = tenantId == null
                ? profileRepository.existsByTenantIdIsNullAndName(request.getName().trim())
                : profileRepository.existsByTenantIdAndName(tenantId, request.getName().trim());
        if (nameExists) {
            throw new IllegalArgumentException("Já existe um perfil com este nome para esta empresa.");
        }
        Profile profile = Profile.builder()
                .tenantId(tenantId)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .permissions(resolvePermissions(request.getPermissionIds()))
                .build();
        profile = profileRepository.save(profile);
        return toResponse(profileRepository.findByIdWithPermissions(profile.getId()).orElse(profile));
    }

    @Transactional
    public ProfileResponse update(UUID id, ProfileRequest request) {
        Profile profile = profileRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil", id));
        if (!SecurityUtils.isCurrentUserRoot() && profile.getTenantId() != null
                && !profile.getTenantId().equals(SecurityUtils.requireTenantId())) {
            throw new ResourceNotFoundException("Perfil", id);
        }
        UUID tenantId = resolveTenantId(request.getTenantId());
        boolean nameExists = tenantId == null
                ? profileRepository.existsByTenantIdIsNullAndName(request.getName().trim())
                : profileRepository.existsByTenantIdAndNameAndIdNot(tenantId, request.getName().trim(), id);
        if (nameExists) {
            throw new IllegalArgumentException("Já existe um perfil com este nome para esta empresa.");
        }
        profile.setTenantId(tenantId);
        profile.setName(request.getName().trim());
        profile.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        profile.setPermissions(resolvePermissions(request.getPermissionIds()));
        profile = profileRepository.save(profile);
        return toResponse(profileRepository.findByIdWithPermissions(profile.getId()).orElse(profile));
    }

    @Transactional
    public void delete(UUID id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil", id));
        if (!SecurityUtils.isCurrentUserRoot() && profile.getTenantId() != null
                && !profile.getTenantId().equals(SecurityUtils.requireTenantId())) {
            throw new ResourceNotFoundException("Perfil", id);
        }
        profileRepository.delete(profile);
    }

    private Set<Permission> resolvePermissions(Set<UUID> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }
        return permissionIds.stream()
                .map(permissionRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toSet());
    }

    private ProfileResponse toResponse(Profile p) {
        Set<PermissionResponse> perms = p.getPermissions() == null ? Set.of() : p.getPermissions().stream()
                .map(perm -> PermissionResponse.builder()
                        .id(perm.getId())
                        .code(perm.getCode())
                        .name(perm.getName())
                        .description(perm.getDescription())
                        .module(perm.getModule())
                        .build())
                .collect(Collectors.toSet());
        return ProfileResponse.builder()
                .id(p.getId())
                .tenantId(p.getTenantId())
                .name(p.getName())
                .description(p.getDescription())
                .permissions(perms)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
