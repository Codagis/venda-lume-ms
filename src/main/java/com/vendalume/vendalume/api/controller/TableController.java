package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.table.*;
import com.vendalume.vendalume.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de mesas e seções.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_TABLES, description = "Mesas e seções de restaurante")
@DefaultApiResponses
@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @Operation(summary = "Criar seção")
    @PostMapping("/sections")
    public ResponseEntity<TableSectionResponse> createSection(@Valid @RequestBody TableSectionCreateRequest request) {
        TableSectionResponse response = tableService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar seção por ID")
    @GetMapping("/sections/{id}")
    public ResponseEntity<TableSectionResponse> findSectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(tableService.findSectionById(id));
    }

    @Operation(summary = "Listar seções")
    @GetMapping("/sections")
    public ResponseEntity<List<TableSectionResponse>> listSections(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(tableService.listSectionsByTenant(tenantId));
    }

    @Operation(summary = "Buscar seções com filtros")
    @PostMapping("/sections/search")
    public ResponseEntity<PageResponse<TableSectionResponse>> searchSections(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody TableSectionFilterRequest filter) {
        return ResponseEntity.ok(tableService.searchSections(tenantId, filter));
    }

    @Operation(summary = "Atualizar seção")
    @PutMapping("/sections/{id}")
    public ResponseEntity<TableSectionResponse> updateSection(
            @PathVariable UUID id,
            @Valid @RequestBody TableSectionUpdateRequest request) {
        return ResponseEntity.ok(tableService.updateSection(id, request));
    }

    @Operation(summary = "Excluir seção")
    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable UUID id) {
        tableService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Criar mesa")
    @PostMapping
    public ResponseEntity<RestaurantTableResponse> createTable(@Valid @RequestBody RestaurantTableCreateRequest request) {
        RestaurantTableResponse response = tableService.createTable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar mesa por ID")
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTableResponse> findTableById(@PathVariable UUID id) {
        return ResponseEntity.ok(tableService.findTableById(id));
    }

    @Operation(summary = "Listar mesas por seção")
    @GetMapping
    public ResponseEntity<List<RestaurantTableResponse>> listTablesBySection(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam UUID sectionId) {
        return ResponseEntity.ok(tableService.listTablesBySection(tenantId, sectionId));
    }

    @Operation(summary = "Buscar mesas com filtros")
    @PostMapping("/search")
    public ResponseEntity<PageResponse<RestaurantTableResponse>> searchTables(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody RestaurantTableFilterRequest filter) {
        return ResponseEntity.ok(tableService.searchTables(tenantId, filter));
    }

    @Operation(summary = "Atualizar mesa")
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTableResponse> updateTable(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantTableUpdateRequest request) {
        return ResponseEntity.ok(tableService.updateTable(id, request));
    }

    @Operation(summary = "Excluir mesa")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable UUID id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
