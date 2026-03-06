package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.permission.PermissionRequest;
import com.vendalume.vendalume.api.dto.permission.PermissionResponse;
import com.vendalume.vendalume.domain.entity.Permission;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.repository.PermissionRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço de gestão de permissões.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    private void requireRoot() {
        if (!SecurityUtils.isCurrentUserRoot()) {
            throw new IllegalStateException("Acesso negado. Apenas usuário root pode gerenciar permissões.");
        }
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listAll() {
        return permissionRepository.findAllByOrderByModuleAscCodeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listByModule(String module) {
        return permissionRepository.findByModuleOrderByCodeAsc(module).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PermissionResponse findById(UUID id) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão", id));
        return toResponse(p);
    }

    @Transactional
    public PermissionResponse create(PermissionRequest request) {
        requireRoot();
        if (permissionRepository.existsByCode(request.getCode().trim())) {
            throw new IllegalArgumentException("Código de permissão já existe: " + request.getCode());
        }
        Permission p = new Permission();
        p.setCode(request.getCode().trim().toUpperCase());
        p.setName(request.getName().trim());
        p.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        p.setModule(request.getModule() != null ? request.getModule().trim().toUpperCase() : null);
        p = permissionRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public PermissionResponse update(UUID id, PermissionRequest request) {
        requireRoot();
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão", id));
        if (!p.getCode().equals(request.getCode().trim()) && permissionRepository.existsByCode(request.getCode().trim())) {
            throw new IllegalArgumentException("Código de permissão já existe: " + request.getCode());
        }
        p.setCode(request.getCode().trim().toUpperCase());
        p.setName(request.getName().trim());
        p.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        p.setModule(request.getModule() != null ? request.getModule().trim().toUpperCase() : null);
        p = permissionRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void delete(UUID id) {
        requireRoot();
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão", id));
        permissionRepository.delete(p);
    }

    private PermissionResponse toResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .module(p.getModule())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
