package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.register.AssignOperatorsRequest;
import com.vendalume.vendalume.api.dto.register.CashierOption;
import com.vendalume.vendalume.api.dto.register.RegisterRequest;
import com.vendalume.vendalume.api.dto.register.RegisterResponse;
import com.vendalume.vendalume.service.RegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/registers")
@RequiredArgsConstructor
@Tag(name = "Pontos de Venda", description = "Cadastro de caixas (PDV) e atribuição de operadores")
public class RegisterController {

    private final RegisterService registerService;

    @GetMapping
    @Operation(summary = "Listar pontos de venda")
    public ResponseEntity<List<RegisterResponse>> list(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        List<RegisterResponse> list = activeOnly
                ? registerService.listActiveByTenant(tenantId)
                : registerService.listByTenant(tenantId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ponto de venda por ID")
    public ResponseEntity<RegisterResponse> getById(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerService.getById(id, tenantId));
    }

    @PostMapping
    @Operation(summary = "Criar ponto de venda")
    public ResponseEntity<RegisterResponse> create(
            @Valid @RequestBody RegisterRequest request,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerService.create(request, tenantId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ponto de venda")
    public ResponseEntity<RegisterResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RegisterRequest request,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerService.update(id, request, tenantId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir ponto de venda")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID tenantId) {
        registerService.delete(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/operators")
    @Operation(summary = "Atribuir operadores ao caixa")
    public ResponseEntity<RegisterResponse> assignOperators(
            @PathVariable UUID id,
            @Valid @RequestBody AssignOperatorsRequest request,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerService.assignOperators(id, request, tenantId));
    }

    @GetMapping("/cashiers")
    @Operation(summary = "Listar operadores de caixa (usuários com perfil Caixa/Operador)")
    public ResponseEntity<List<CashierOption>> listCashiers(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerService.listCashiersByTenant(tenantId));
    }
}
