package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.cardmachine.CardMachineRequest;
import com.vendalume.vendalume.api.dto.cardmachine.CardMachineResponse;
import com.vendalume.vendalume.service.CardMachineService;
import com.vendalume.vendalume.security.SecurityUtils;
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
@Tag(name = "Maquininhas", description = "CRUD de maquininhas por empresa")
public class CardMachineController {

    private final CardMachineService cardMachineService;

    @GetMapping("/{tenantId}/card-machines")
    @Operation(summary = "Listar maquininhas da empresa")
    public ResponseEntity<List<CardMachineResponse>> list(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(cardMachineService.listByTenant(tenantId));
    }

    @GetMapping("/current/card-machines")
    @Operation(summary = "Listar maquininhas da empresa do usuario logado")
    public ResponseEntity<List<CardMachineResponse>> listCurrent() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return ResponseEntity.ok(cardMachineService.listByTenant(tenantId));
    }

    @GetMapping("/{tenantId}/card-machines/active")
    @Operation(summary = "Listar maquininhas ativas (para PDV)")
    public ResponseEntity<List<CardMachineResponse>> listActive(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(cardMachineService.listActiveByTenant(tenantId));
    }

    @GetMapping("/current/card-machines/active")
    @Operation(summary = "Listar maquininhas ativas da empresa do usuario (para PDV)")
    public ResponseEntity<List<CardMachineResponse>> listCurrentActive() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return ResponseEntity.ok(cardMachineService.listActiveByTenant(tenantId));
    }

    @GetMapping("/{tenantId}/card-machines/{id}")
    @Operation(summary = "Buscar maquininha por ID")
    public ResponseEntity<CardMachineResponse> findById(@PathVariable UUID tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(cardMachineService.findById(tenantId, id));
    }

    @PostMapping("/{tenantId}/card-machines")
    @Operation(summary = "Criar maquininha")
    public ResponseEntity<CardMachineResponse> create(@PathVariable UUID tenantId, @Valid @RequestBody CardMachineRequest request) {
        CardMachineResponse response = cardMachineService.create(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/current/card-machines")
    @Operation(summary = "Criar maquininha na empresa do usuario")
    public ResponseEntity<CardMachineResponse> createCurrent(@Valid @RequestBody CardMachineRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        CardMachineResponse response = cardMachineService.create(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{tenantId}/card-machines/{id}")
    @Operation(summary = "Atualizar maquininha")
    public ResponseEntity<CardMachineResponse> update(
            @PathVariable UUID tenantId, @PathVariable UUID id, @Valid @RequestBody CardMachineRequest request) {
        return ResponseEntity.ok(cardMachineService.update(tenantId, id, request));
    }

    @PutMapping("/current/card-machines/{id}")
    @Operation(summary = "Atualizar maquininha da empresa do usuario")
    public ResponseEntity<CardMachineResponse> updateCurrent(@PathVariable UUID id, @Valid @RequestBody CardMachineRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        return ResponseEntity.ok(cardMachineService.update(tenantId, id, request));
    }

    @DeleteMapping("/{tenantId}/card-machines/{id}")
    @Operation(summary = "Excluir maquininha")
    public ResponseEntity<Void> delete(@PathVariable UUID tenantId, @PathVariable UUID id) {
        cardMachineService.delete(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/current/card-machines/{id}")
    @Operation(summary = "Excluir maquininha da empresa do usuario")
    public ResponseEntity<Void> deleteCurrent(@PathVariable UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        cardMachineService.delete(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
