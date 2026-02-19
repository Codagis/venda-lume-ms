package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.module.ModuleRequest;
import com.vendalume.vendalume.api.dto.module.ModuleResponse;
import com.vendalume.vendalume.service.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API de módulos. GET / retorna os módulos do usuário. CRUD para root.
 */
@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    public ResponseEntity<List<ModuleResponse>> listForCurrentUser() {
        return ResponseEntity.ok(moduleService.listForCurrentUser());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<ModuleResponse>> listAllForAdmin() {
        return ResponseEntity.ok(moduleService.listAllForAdmin());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<ModuleResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(moduleService.getById(id));
    }

    @PostMapping("/admin")
    public ResponseEntity<ModuleResponse> create(@Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.create(request));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<ModuleResponse> update(@PathVariable UUID id, @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.update(id, request));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
