package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.permission.PermissionRequest;
import com.vendalume.vendalume.api.dto.permission.PermissionResponse;
import com.vendalume.vendalume.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissões", description = "Permissões granulares. Listagem para todos; criar/editar apenas root.")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "Listar permissões")
    public ResponseEntity<List<PermissionResponse>> listAll() {
        return ResponseEntity.ok(permissionService.listAll());
    }

    @GetMapping("/module/{module}")
    @Operation(summary = "Listar por módulo")
    public ResponseEntity<List<PermissionResponse>> listByModule(@PathVariable String module) {
        return ResponseEntity.ok(permissionService.listByModule(module));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar permissão por ID")
    public ResponseEntity<PermissionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar permissão (root)")
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar permissão (root)")
    public ResponseEntity<PermissionResponse> update(@PathVariable UUID id, @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir permissão (root)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
