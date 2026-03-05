package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.table.*;
import com.vendalume.vendalume.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    // ---------- TableSection ----------

    @PostMapping("/sections")
    public ResponseEntity<TableSectionResponse> createSection(@Valid @RequestBody TableSectionCreateRequest request) {
        TableSectionResponse response = tableService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sections/{id}")
    public ResponseEntity<TableSectionResponse> findSectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(tableService.findSectionById(id));
    }

    @GetMapping("/sections")
    public ResponseEntity<List<TableSectionResponse>> listSections(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(tableService.listSectionsByTenant(tenantId));
    }

    @PostMapping("/sections/search")
    public ResponseEntity<PageResponse<TableSectionResponse>> searchSections(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody TableSectionFilterRequest filter) {
        return ResponseEntity.ok(tableService.searchSections(tenantId, filter));
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<TableSectionResponse> updateSection(
            @PathVariable UUID id,
            @Valid @RequestBody TableSectionUpdateRequest request) {
        return ResponseEntity.ok(tableService.updateSection(id, request));
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable UUID id) {
        tableService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- RestaurantTable ----------

    @PostMapping
    public ResponseEntity<RestaurantTableResponse> createTable(@Valid @RequestBody RestaurantTableCreateRequest request) {
        RestaurantTableResponse response = tableService.createTable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTableResponse> findTableById(@PathVariable UUID id) {
        return ResponseEntity.ok(tableService.findTableById(id));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantTableResponse>> listTablesBySection(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam UUID sectionId) {
        return ResponseEntity.ok(tableService.listTablesBySection(tenantId, sectionId));
    }

    @PostMapping("/search")
    public ResponseEntity<PageResponse<RestaurantTableResponse>> searchTables(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody RestaurantTableFilterRequest filter) {
        return ResponseEntity.ok(tableService.searchTables(tenantId, filter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTableResponse> updateTable(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantTableUpdateRequest request) {
        return ResponseEntity.ok(tableService.updateTable(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable UUID id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
