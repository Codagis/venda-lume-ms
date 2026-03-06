package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.auth.RegisterRequest;
import com.vendalume.vendalume.api.dto.auth.UserResponse;
import com.vendalume.vendalume.api.dto.auth.UserUpdateRequest;
import com.vendalume.vendalume.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de usuários.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_USERS, description = "Usuários")
@DefaultApiResponses
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Listar usuários")
    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(userService.listForCurrentUser());
    }

    @Operation(summary = "Buscar usuário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "Criar usuário")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @Operation(summary = "Atualizar usuário")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }
}
