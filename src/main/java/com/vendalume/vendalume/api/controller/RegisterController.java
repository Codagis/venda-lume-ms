package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.register.AssignOperatorsRequest;
import com.vendalume.vendalume.api.dto.register.CashierOption;
import com.vendalume.vendalume.api.dto.register.RegisterRequest;
import com.vendalume.vendalume.api.dto.register.RegisterResponse;
import com.vendalume.vendalume.api.dto.register.RegisterSessionDetailResponse;
import com.vendalume.vendalume.api.dto.register.RegisterSessionResponse;
import com.vendalume.vendalume.api.dto.register.StartSessionRequest;
import com.vendalume.vendalume.api.dto.register.VerifyPdvPasswordRequest;
import com.vendalume.vendalume.service.RegisterService;
import com.vendalume.vendalume.service.RegisterSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de pontos de venda (caixas) e atribuição de operadores.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@DefaultApiResponses
@RestController
@RequestMapping("/registers")
@RequiredArgsConstructor
@Tag(name = "Pontos de Venda", description = "Cadastro de caixas (PDV) e atribuição de operadores")
public class RegisterController {

    private final RegisterService registerService;
    private final RegisterSessionService registerSessionService;

    @GetMapping
    @Operation(summary = "Listar pontos de venda. Com activeOnly e forCurrentOperator=true, retorna só os PDVs que o operador pode usar (root vê todos). Envie X-Device-IMEI para filtrar por dispositivo vinculado.")
    public ResponseEntity<List<RegisterResponse>> list(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @RequestParam(required = false, defaultValue = "false") boolean forCurrentOperator,
            @RequestHeader(value = "X-Device-IMEI", required = false) String deviceImei) {
        List<RegisterResponse> list = activeOnly
                ? registerService.listActiveByTenant(tenantId, forCurrentOperator, deviceImei)
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

    @GetMapping("/by-imei")
    @Operation(summary = "Obter ou criar PDV pelo IMEI do equipamento")
    public ResponseEntity<RegisterResponse> getOrCreateByImei(
            @RequestParam String imei,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerService.getOrCreateByImei(imei, tenantId));
    }

    @PostMapping("/{id}/verify-password")
    @Operation(summary = "Verificar senha de acesso do PDV")
    public ResponseEntity<Void> verifyPdvPassword(
            @PathVariable UUID id,
            @Valid @RequestBody VerifyPdvPasswordRequest request,
            @RequestParam(required = false) UUID tenantId) {
        if (!registerService.verifyPdvPassword(id, request.getPassword(), tenantId)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok().build();
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

    @PostMapping("/{id}/session/start")
    @Operation(summary = "Iniciar sessão do PDV (auditoria). Se o PDV tiver senha, envie pdvPassword no body.")
    public ResponseEntity<RegisterSessionResponse> startSession(
            @PathVariable UUID id,
            @RequestBody(required = false) StartSessionRequest request,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerSessionService.startSession(id, request, tenantId));
    }

    @PostMapping("/{id}/session/end")
    @Operation(summary = "Encerrar sessão do PDV")
    public ResponseEntity<RegisterSessionResponse> endSession(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerSessionService.endSession(id, tenantId));
    }

    @GetMapping("/{id}/sessions")
    @Operation(summary = "Listar histórico de sessões do caixa")
    public ResponseEntity<List<RegisterSessionResponse>> listSessions(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerSessionService.listSessionsByRegister(id, tenantId));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Detalhe da sessão do PDV com vendas")
    public ResponseEntity<RegisterSessionDetailResponse> getSessionDetail(
            @PathVariable UUID sessionId,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(registerSessionService.getSessionDetail(sessionId, tenantId));
    }
}
