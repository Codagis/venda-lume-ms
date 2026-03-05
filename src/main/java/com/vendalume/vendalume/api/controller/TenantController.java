package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.tenant.TenantRequest;
import com.vendalume.vendalume.api.dto.tenant.TenantResponse;
import com.vendalume.vendalume.service.TenantService;
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
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "CRUD de empresas (tenants). Apenas usuário root.")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @Operation(summary = "Listar empresas")
    public ResponseEntity<List<TenantResponse>> listAll() {
        return ResponseEntity.ok(tenantService.listAll());
    }

    @GetMapping("/current")
    @Operation(summary = "Buscar empresa do usuário logado")
    public ResponseEntity<TenantResponse> getCurrentTenant() {
        return ResponseEntity.ok(tenantService.getCurrentUserTenant());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar empresa por ID")
    public ResponseEntity<TenantResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar empresa")
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody TenantRequest request) {
        TenantResponse response = tenantService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar empresa")
    public ResponseEntity<TenantResponse> update(@PathVariable UUID id, @Valid @RequestBody TenantRequest request) {
        return ResponseEntity.ok(tenantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir empresa")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
