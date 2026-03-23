package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller de controle de custos (contas a pagar e receber).
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_COST_CONTROL, description = "Contas a pagar e receber")
@DefaultApiResponses
@RestController
@RequestMapping("/cost-control")
@RequiredArgsConstructor
public class CostControlController {

    private final CostControlService costControlService;
    private final CostControlReportService costControlReportService;

    @Operation(summary = "Criar conta a pagar")
    @PostMapping("/payables")
    public ResponseEntity<AccountPayableResponse> createPayable(@Valid @RequestBody AccountPayableCreateRequest request) {
        AccountPayableResponse response = costControlService.createPayable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar conta a pagar por ID")
    @GetMapping("/payables/{id}")
    public ResponseEntity<AccountPayableResponse> findPayableById(@PathVariable UUID id) {
        return ResponseEntity.ok(costControlService.findPayableById(id));
    }

    @Operation(summary = "Buscar contas a pagar com filtros")
    @PostMapping("/payables/search")
    public ResponseEntity<PageResponse<AccountPayableResponse>> searchPayables(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody AccountPayableFilterRequest filter) {
        return ResponseEntity.ok(costControlService.searchPayables(tenantId, filter));
    }

    @Operation(summary = "Exportar contas a pagar em Excel")
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

    @Operation(summary = "Exportar contas a pagar em PDF")
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

    @Operation(summary = "Atualizar conta a pagar")
    @PutMapping("/payables/{id}")
    public ResponseEntity<AccountPayableResponse> updatePayable(
            @PathVariable UUID id,
            @Valid @RequestBody AccountPayableUpdateRequest request) {
        return ResponseEntity.ok(costControlService.updatePayable(id, request));
    }

    @Operation(summary = "Registrar pagamento de conta a pagar")
    @PostMapping("/payables/{id}/payment")
    public ResponseEntity<AccountPayableResponse> registerPayablePayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRegistrationRequest request) {
        return ResponseEntity.ok(costControlService.registerPayablePayment(id, request));
    }

    @Operation(summary = "Excluir conta a pagar")
    @DeleteMapping("/payables/{id}")
    public ResponseEntity<Void> deletePayable(@PathVariable UUID id) {
        costControlService.deletePayable(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gerar comprovante de pagamento (PDF) da conta a pagar")
    @GetMapping("/payables/{id}/payment-receipt")
    public ResponseEntity<byte[]> getPaymentReceiptPdf(@PathVariable UUID id) {
        byte[] content = costControlReportService.generatePaymentReceiptPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprovante-pagamento.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    @Operation(summary = "Criar conta a receber")
    @PostMapping("/receivables")
    public ResponseEntity<AccountReceivableResponse> createReceivable(@Valid @RequestBody AccountReceivableCreateRequest request) {
        AccountReceivableResponse response = costControlService.createReceivable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar conta a receber por ID")
    @GetMapping("/receivables/{id}")
    public ResponseEntity<AccountReceivableResponse> findReceivableById(@PathVariable UUID id) {
        return ResponseEntity.ok(costControlService.findReceivableById(id));
    }

    @Operation(summary = "Buscar contas a receber com filtros")
    @PostMapping("/receivables/search")
    public ResponseEntity<PageResponse<AccountReceivableResponse>> searchReceivables(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody AccountReceivableFilterRequest filter) {
        return ResponseEntity.ok(costControlService.searchReceivables(tenantId, filter));
    }

    @Operation(summary = "Exportar contas a receber em Excel")
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

    @Operation(summary = "Exportar contas a receber em PDF")
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

    @Operation(summary = "Atualizar conta a receber")
    @PutMapping("/receivables/{id}")
    public ResponseEntity<AccountReceivableResponse> updateReceivable(
            @PathVariable UUID id,
            @Valid @RequestBody AccountReceivableUpdateRequest request) {
        return ResponseEntity.ok(costControlService.updateReceivable(id, request));
    }

    @Operation(summary = "Registrar pagamento de conta a receber")
    @PostMapping("/receivables/{id}/payment")
    public ResponseEntity<AccountReceivableResponse> registerReceivablePayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRegistrationRequest request) {
        return ResponseEntity.ok(costControlService.registerReceivablePayment(id, request));
    }

    @Operation(summary = "Excluir conta a receber")
    @DeleteMapping("/receivables/{id}")
    public ResponseEntity<Void> deleteReceivable(@PathVariable UUID id) {
        costControlService.deleteReceivable(id);
        return ResponseEntity.noContent().build();
    }
}
