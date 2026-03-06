package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.stock.StockFilterRequest;
import com.vendalume.vendalume.api.dto.stock.StockMovementRequest;
import com.vendalume.vendalume.api.dto.stock.StockMovementResponse;
import com.vendalume.vendalume.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de estoque e movimentações.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_STOCK, description = "Estoque e movimentações")
@DefaultApiResponses
@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "Registrar movimentação de estoque")
    @PostMapping("/movements")
    public ResponseEntity<StockMovementResponse> registerMovement(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(stockService.registerMovement(tenantId, request));
    }

    @Operation(summary = "Buscar movimentações com filtros")
    @PostMapping("/movements/search")
    public ResponseEntity<PageResponse<StockMovementResponse>> searchMovements(
            @RequestParam(required = false) UUID tenantId,
            @RequestBody StockFilterRequest filter) {
        return ResponseEntity.ok(stockService.listMovements(tenantId, filter));
    }

    @Operation(summary = "Listar movimentações por produto")
    @GetMapping("/movements/product/{productId}")
    public ResponseEntity<List<StockMovementResponse>> getMovementsByProduct(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(stockService.listMovementsByProduct(tenantId, productId, limit));
    }
}
