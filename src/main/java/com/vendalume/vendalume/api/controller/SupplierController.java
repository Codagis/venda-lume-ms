package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.supplier.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller de fornecedores.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_SUPPLIERS, description = "Fornecedores")
@DefaultApiResponses
@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "Criar fornecedor")
    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierCreateRequest request) {
        SupplierResponse response = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar fornecedor por ID")
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @Operation(summary = "Buscar fornecedores com filtros")
    @PostMapping("/search")
    public ResponseEntity<PageResponse<SupplierResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody SupplierFilterRequest filter) {
        return ResponseEntity.ok(supplierService.search(tenantId, filter));
    }

    @Operation(summary = "Atualizar fornecedor")
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierUpdateRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @Operation(summary = "Excluir fornecedor")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
