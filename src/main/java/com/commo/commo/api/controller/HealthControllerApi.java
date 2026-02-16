package com.commo.commo.api.controller;

import com.commo.commo.api.documentation.ApiDocumentedController;
import com.commo.commo.api.documentation.DefaultApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * Interface do controller de health check.
 * Contém as anotações de endpoint e assinaturas dos métodos.
 * O controller deve usar @RequestMapping("/health").
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_HEALTH, description = "Verificação de status da aplicação")
@DefaultApiResponses
@SecurityRequirements
public interface HealthControllerApi {

    @Operation(summary = "Health check", description = "Retorna o status da aplicação (UP/DOWN)")
    @ApiResponse(responseCode = "200", description = "Aplicação em execução")
    @GetMapping
    ResponseEntity<Map<String, String>> health();
}
