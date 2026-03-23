package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.employee.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Funcionários", description = "Cadastro de funcionários e folha de pagamento")
@DefaultApiResponses
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Criar funcionário")
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar funcionário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @Operation(summary = "Buscar funcionários com filtros")
    @PostMapping("/search")
    public ResponseEntity<PageResponse<EmployeeResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody EmployeeFilterRequest filter) {
        return ResponseEntity.ok(employeeService.search(tenantId, filter));
    }

    @Operation(summary = "Atualizar funcionário")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @Operation(summary = "Excluir funcionário")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
