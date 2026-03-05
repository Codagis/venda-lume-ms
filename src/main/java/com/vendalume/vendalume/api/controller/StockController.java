package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.stock.StockFilterRequest;
import com.vendalume.vendalume.api.dto.stock.StockMovementRequest;
import com.vendalume.vendalume.api.dto.stock.StockMovementResponse;
import com.vendalume.vendalume.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/movements")
    public ResponseEntity<StockMovementResponse> registerMovement(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(stockService.registerMovement(tenantId, request));
    }

    @PostMapping("/movements/search")
    public ResponseEntity<PageResponse<StockMovementResponse>> searchMovements(
            @RequestParam(required = false) UUID tenantId,
            @RequestBody StockFilterRequest filter) {
        return ResponseEntity.ok(stockService.listMovements(tenantId, filter));
    }

    @GetMapping("/movements/product/{productId}")
    public ResponseEntity<List<StockMovementResponse>> getMovementsByProduct(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(stockService.listMovementsByProduct(tenantId, productId, limit));
    }
}
