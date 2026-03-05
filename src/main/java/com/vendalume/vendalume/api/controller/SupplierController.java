package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.supplier.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierCreateRequest request) {
        SupplierResponse response = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PostMapping("/search")
    public ResponseEntity<PageResponse<SupplierResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody SupplierFilterRequest filter) {
        return ResponseEntity.ok(supplierService.search(tenantId, filter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierUpdateRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
