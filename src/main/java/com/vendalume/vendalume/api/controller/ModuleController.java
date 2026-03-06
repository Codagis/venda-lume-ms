package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.module.ModuleRequest;
import com.vendalume.vendalume.api.dto.module.ModuleResponse;
import com.vendalume.vendalume.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de módulos.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_MODULES, description = "Módulos do sistema")
@DefaultApiResponses
@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @Operation(summary = "Listar módulos do usuário")
    @GetMapping
    public ResponseEntity<List<ModuleResponse>> listForCurrentUser() {
        return ResponseEntity.ok(moduleService.listForCurrentUser());
    }

    @Operation(summary = "Listar todos os módulos (admin)")
    @GetMapping("/admin")
    public ResponseEntity<List<ModuleResponse>> listAllForAdmin() {
        return ResponseEntity.ok(moduleService.listAllForAdmin());
    }

    @Operation(summary = "Buscar módulo por ID")
    @GetMapping("/admin/{id}")
    public ResponseEntity<ModuleResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(moduleService.getById(id));
    }

    @Operation(summary = "Criar módulo")
    @PostMapping("/admin")
    public ResponseEntity<ModuleResponse> create(@Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.create(request));
    }

    @Operation(summary = "Atualizar módulo")
    @PutMapping("/admin/{id}")
    public ResponseEntity<ModuleResponse> update(@PathVariable UUID id, @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.update(id, request));
    }

    @Operation(summary = "Excluir módulo")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
