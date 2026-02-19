package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.module.ModuleRequest;
import com.vendalume.vendalume.api.dto.module.ModuleResponse;
import com.vendalume.vendalume.domain.entity.Module;
import com.vendalume.vendalume.domain.entity.Permission;
import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.repository.ModuleRepository;
import com.vendalume.vendalume.repository.ProfileRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de módulos. Retorna apenas os módulos que o usuário tem permissão para visualizar.
 * Root vê todos; demais usuários veem apenas módulos cuja view_permission_code está no perfil.
 */
@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ProfileRepository profileRepository;

    /**
     * Lista módulos disponíveis para o usuário atual.
     * Root: todos os módulos ativos.
     * Outros: módulos cuja permissão de visualização está no perfil do usuário.
     */
    public List<ModuleResponse> listForCurrentUser() {
        User user = SecurityUtils.requireCurrentUser();

        if (Boolean.TRUE.equals(user.getIsRoot())) {
            return moduleRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                    .map(this::toResponse)
                    .toList();
        }

        Set<String> permissionCodes = getPermissionCodesForUser(user);
        return moduleRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .filter(m -> permissionCodes.contains(m.getViewPermissionCode()))
                .map(this::toResponse)
                .toList();
    }

    private Set<String> getPermissionCodesForUser(User user) {
        if (user.getProfileId() == null) {
            return Set.of();
        }
        return Optional.ofNullable(profileRepository.findByIdWithPermissions(user.getProfileId()).orElse(null))
                .map(p -> p.getPermissions() == null ? Set.<Permission>of() : p.getPermissions())
                .orElse(Set.of())
                .stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    /** Lista todos os módulos para administração (somente root). */
    public List<ModuleResponse> listAllForAdmin() {
        if (!SecurityUtils.isCurrentUserRoot()) {
            throw new IllegalArgumentException("Acesso negado. Apenas root.");
        }
        return moduleRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getDisplayOrder() != null ? a.getDisplayOrder() : 0,
                        b.getDisplayOrder() != null ? b.getDisplayOrder() : 0))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ModuleResponse create(ModuleRequest req) {
        if (!SecurityUtils.isCurrentUserRoot()) {
            throw new IllegalArgumentException("Acesso negado. Apenas root.");
        }
        if (moduleRepository.existsByCode(req.getCode().trim())) {
            throw new IllegalArgumentException("Código de módulo já existe");
        }
        Module m = new Module();
        m.setCode(req.getCode().trim().toUpperCase());
        m.setName(req.getName().trim());
        m.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        m.setIcon(req.getIcon() != null ? req.getIcon().trim() : null);
        m.setRoute(req.getRoute().trim());
        m.setComponent(req.getComponent().trim());
        m.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        m.setViewPermissionCode(req.getViewPermissionCode().trim());
        m.setActive(req.getActive() != null ? req.getActive() : true);
        m = moduleRepository.save(m);
        return toResponse(m);
    }

    @Transactional
    public ModuleResponse update(UUID id, ModuleRequest req) {
        if (!SecurityUtils.isCurrentUserRoot()) {
            throw new IllegalArgumentException("Acesso negado. Apenas root.");
        }
        Module m = moduleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Módulo não encontrado"));
        if (!m.getCode().equals(req.getCode().trim().toUpperCase()) && moduleRepository.existsByCode(req.getCode().trim())) {
            throw new IllegalArgumentException("Código de módulo já existe");
        }
        m.setCode(req.getCode().trim().toUpperCase());
        m.setName(req.getName().trim());
        m.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        m.setIcon(req.getIcon() != null ? req.getIcon().trim() : null);
        m.setRoute(req.getRoute().trim());
        m.setComponent(req.getComponent().trim());
        m.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        m.setViewPermissionCode(req.getViewPermissionCode().trim());
        if (req.getActive() != null) m.setActive(req.getActive());
        m = moduleRepository.save(m);
        return toResponse(m);
    }

    @Transactional
    public void delete(UUID id) {
        if (!SecurityUtils.isCurrentUserRoot()) {
            throw new IllegalArgumentException("Acesso negado. Apenas root.");
        }
        Module m = moduleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Módulo não encontrado"));
        moduleRepository.delete(m);
    }

    public ModuleResponse getById(UUID id) {
        if (!SecurityUtils.isCurrentUserRoot()) {
            throw new IllegalArgumentException("Acesso negado. Apenas root.");
        }
        Module m = moduleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Módulo não encontrado"));
        return toResponse(m);
    }

    private ModuleResponse toResponse(Module m) {
        return ModuleResponse.builder()
                .id(m.getId())
                .code(m.getCode())
                .name(m.getName())
                .description(m.getDescription())
                .icon(m.getIcon())
                .route(m.getRoute())
                .component(m.getComponent())
                .displayOrder(m.getDisplayOrder())
                .viewPermissionCode(m.getViewPermissionCode())
                .active(m.getActive())
                .build();
    }
}
