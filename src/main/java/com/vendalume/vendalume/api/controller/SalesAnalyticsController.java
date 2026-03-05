package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.analytics.SalesAnalyticsResponse;
import com.vendalume.vendalume.service.SalesAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/analytics/sales")
@RequiredArgsConstructor
@Tag(name = "Análise de Vendas", description = "Estratégias, produtos mais vendidos e segmentos")
public class SalesAnalyticsController {

    private final SalesAnalyticsService salesAnalyticsService;

    @GetMapping
    @Operation(summary = "Obter análise de vendas e estratégias", description = "Produtos mais vendidos, segmentos e recomendações")
    public ResponseEntity<SalesAnalyticsResponse> getAnalytics(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(30);
        if (start.isAfter(end)) {
            start = end.minusDays(30);
        }

        return ResponseEntity.ok(salesAnalyticsService.getAnalytics(tenantId, start, end));
    }
}
