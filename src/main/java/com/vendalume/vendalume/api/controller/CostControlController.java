package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.costcontrol.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.service.CostControlReportService;
import com.vendalume.vendalume.service.CostControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cost-control")
@RequiredArgsConstructor
public class CostControlController {

    private final CostControlService costControlService;
    private final CostControlReportService costControlReportService;

    @PostMapping("/payables")
    public ResponseEntity<AccountPayableResponse> createPayable(@Valid @RequestBody AccountPayableCreateRequest request) {
        AccountPayableResponse response = costControlService.createPayable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/payables/{id}")
    public ResponseEntity<AccountPayableResponse> findPayableById(@PathVariable UUID id) {
        return ResponseEntity.ok(costControlService.findPayableById(id));
    }

    @PostMapping("/payables/search")
    public ResponseEntity<PageResponse<AccountPayableResponse>> searchPayables(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody AccountPayableFilterRequest filter) {
        return ResponseEntity.ok(costControlService.searchPayables(tenantId, filter));
    }

    @PostMapping("/payables/report/excel")
    public ResponseEntity<byte[]> exportPayablesExcel(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody AccountPayableFilterRequest filter) {
        byte[] content = costControlReportService.generatePayablesExcel(tenantId, filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contas-a-pagar.xls\"")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(content);
    }

    @PostMapping("/payables/report/pdf")
    public ResponseEntity<byte[]> exportPayablesPdf(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody AccountPayableFilterRequest filter) {
        byte[] content = costControlReportService.generatePayablesPdf(tenantId, filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contas-a-pagar.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    @PutMapping("/payables/{id}")
    public ResponseEntity<AccountPayableResponse> updatePayable(
            @PathVariable UUID id,
            @Valid @RequestBody AccountPayableUpdateRequest request) {
        return ResponseEntity.ok(costControlService.updatePayable(id, request));
    }

    @PostMapping("/payables/{id}/payment")
    public ResponseEntity<AccountPayableResponse> registerPayablePayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRegistrationRequest request) {
        return ResponseEntity.ok(costControlService.registerPayablePayment(id, request));
    }

    @DeleteMapping("/payables/{id}")
    public ResponseEntity<Void> deletePayable(@PathVariable UUID id) {
        costControlService.deletePayable(id);
        return ResponseEntity.noContent().build();
    }

    // --- Contas a Receber ---
    @PostMapping("/receivables")
    public ResponseEntity<AccountReceivableResponse> createReceivable(@Valid @RequestBody AccountReceivableCreateRequest request) {
        AccountReceivableResponse response = costControlService.createReceivable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/receivables/{id}")
    public ResponseEntity<AccountReceivableResponse> findReceivableById(@PathVariable UUID id) {
        return ResponseEntity.ok(costControlService.findReceivableById(id));
    }

    @PostMapping("/receivables/search")
    public ResponseEntity<PageResponse<AccountReceivableResponse>> searchReceivables(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody AccountReceivableFilterRequest filter) {
        return ResponseEntity.ok(costControlService.searchReceivables(tenantId, filter));
    }

    @PostMapping("/receivables/report/excel")
    public ResponseEntity<byte[]> exportReceivablesExcel(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody AccountReceivableFilterRequest filter) {
        byte[] content = costControlReportService.generateReceivablesExcel(tenantId, filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contas-a-receber.xls\"")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(content);
    }

    @PostMapping("/receivables/report/pdf")
    public ResponseEntity<byte[]> exportReceivablesPdf(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody AccountReceivableFilterRequest filter) {
        byte[] content = costControlReportService.generateReceivablesPdf(tenantId, filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contas-a-receber.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    @PutMapping("/receivables/{id}")
    public ResponseEntity<AccountReceivableResponse> updateReceivable(
            @PathVariable UUID id,
            @Valid @RequestBody AccountReceivableUpdateRequest request) {
        return ResponseEntity.ok(costControlService.updateReceivable(id, request));
    }

    @PostMapping("/receivables/{id}/payment")
    public ResponseEntity<AccountReceivableResponse> registerReceivablePayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRegistrationRequest request) {
        return ResponseEntity.ok(costControlService.registerReceivablePayment(id, request));
    }

    @DeleteMapping("/receivables/{id}")
    public ResponseEntity<Void> deleteReceivable(@PathVariable UUID id) {
        costControlService.deleteReceivable(id);
        return ResponseEntity.noContent().build();
    }
}
