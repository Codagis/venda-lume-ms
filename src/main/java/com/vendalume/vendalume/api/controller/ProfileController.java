package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.profile.ProfileRequest;
import com.vendalume.vendalume.api.dto.profile.ProfileResponse;
import com.vendalume.vendalume.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de perfis de acesso.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@DefaultApiResponses
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Tag(name = "Perfis", description = "Perfis de acesso (conjunto de permissões) por empresa.")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Listar perfis por empresa")
    public ResponseEntity<List<ProfileResponse>> listByTenant(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(profileService.listByTenant(tenantId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar perfil por ID")
    public ResponseEntity<ProfileResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(profileService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar perfil")
    public ResponseEntity<ProfileResponse> create(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar perfil")
    public ResponseEntity<ProfileResponse> update(@PathVariable UUID id, @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir perfil")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
