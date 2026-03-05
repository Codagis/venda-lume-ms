package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.sale.SaleCreateRequest;
import com.vendalume.vendalume.api.dto.sale.SaleFilterRequest;
import com.vendalume.vendalume.api.dto.sale.SaleResponse;
import com.vendalume.vendalume.api.dto.sale.SaleSummaryResponse;
import com.vendalume.vendalume.api.dto.sale.SaleAuditResponse;
import com.vendalume.vendalume.api.dto.sale.SaleCustomerUpdateRequest;
import com.vendalume.vendalume.api.dto.sale.SalePaymentUpdateRequest;
import com.vendalume.vendalume.service.SaleFiscalNfeService;
import com.vendalume.vendalume.service.SaleFiscalNfceService;
import com.vendalume.vendalume.service.SaleReceiptPdfService;
import com.vendalume.vendalume.service.SaleReportService;
import com.vendalume.vendalume.service.SaleSimpleReceiptPdfService;
import com.vendalume.vendalume.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final SaleReceiptPdfService saleReceiptPdfService;
    private final SaleSimpleReceiptPdfService saleSimpleReceiptPdfService;
    private final SaleFiscalNfceService saleFiscalNfceService;
    private final SaleFiscalNfeService saleFiscalNfeService;
    private final SaleReportService saleReportService;

    @PostMapping
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleCreateRequest request) {
        SaleResponse response = saleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(saleService.getById(id));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<SaleAuditResponse>> getAudit(@PathVariable UUID id) {
        return ResponseEntity.ok(saleService.getAudit(id));
    }

    /**
     * Atualiza apenas o código de autorização do cartão (vendas com pagamento crédito/débito).
     * Alteração auditada.
     */
    @PatchMapping("/{id}/card-authorization")
    public ResponseEntity<SaleResponse> updateCardAuthorization(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String cardAuthorization = body != null ? body.get("cardAuthorization") : null;
        return ResponseEntity.ok(saleService.updateCardAuthorization(id, cardAuthorization));
    }

    /**
     * Adiciona o pagamento a uma venda pendente (status OPEN) e conclui a venda.
     * Após isso o usuário pode gerar cupom fiscal, NF-e e comprovante.
     */
    @PatchMapping("/{id}/payment")
    public ResponseEntity<SaleResponse> addPayment(
            @PathVariable UUID id,
            @Valid @RequestBody SalePaymentUpdateRequest request) {
        return ResponseEntity.ok(saleService.addPayment(id, request));
    }

    /**
     * Atualiza cliente da venda (nome e CPF/CNPJ). Alteração auditada.
     */
    @PatchMapping("/{id}/customer")
    public ResponseEntity<SaleResponse> updateCustomer(
            @PathVariable UUID id,
            @RequestBody SaleCustomerUpdateRequest request) {
        return ResponseEntity.ok(saleService.updateSaleCustomer(id, request));
    }

    @PostMapping("/search")
    public ResponseEntity<PageResponse<SaleResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody SaleFilterRequest filter) {
        return ResponseEntity.ok(saleService.search(tenantId, filter));
    }

    @PostMapping("/summary")
    public ResponseEntity<SaleSummaryResponse> getSummary(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody SaleFilterRequest filter) {
        return ResponseEntity.ok(saleService.getSummary(tenantId, filter));
    }

    @GetMapping(value = "/{id}/receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getReceiptPdf(@PathVariable UUID id) {
        byte[] pdf = saleReceiptPdfService.generateReceiptPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cupom-fiscal-venda-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping(value = "/{id}/simple-receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getSimpleReceiptPdf(@PathVariable UUID id) {
        byte[] pdf = saleSimpleReceiptPdfService.generateSimpleReceiptPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprovante-venda-" + id + ".pdf\"")
                .body(pdf);
    }

    /**
     * Emite NFC-e (cupom fiscal completo) via Fiscal Simplify e retorna o PDF oficial da SEFAZ.
     * Requer empresa cadastrada no Fiscal Simplify (CNPJ, código município, IE).
     */
    @GetMapping(value = "/{id}/fiscal-receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getFiscalReceiptPdf(@PathVariable UUID id) {
        byte[] pdf = saleFiscalNfceService.emitirNfceEPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cupom-fiscal-nfce-" + id + ".pdf\"")
                .body(pdf);
    }

    /**
     * Emite NF-e (Nota Fiscal Eletrônica) via Fiscal Simplify, grava chave/número na venda e retorna o PDF (DANFE).
     */
    @GetMapping(value = "/{id}/nfe.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getNfePdf(@PathVariable UUID id) {
        byte[] pdf = saleFiscalNfeService.emitirNfeEPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nfe-" + id + ".pdf\"")
                .body(pdf);
    }

    /**
     * Exporta relatório de vendas em Excel (XLS) conforme filtros.
     */
    @PostMapping("/report/excel")
    public ResponseEntity<byte[]> exportReportExcel(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody SaleFilterRequest filter) {
        byte[] content = saleReportService.generateExcelReport(tenantId, filter);
        String filename = "relatorio-vendas.xls";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(content);
    }

    /**
     * Exporta relatório de vendas em PDF conforme filtros (layout profissional com logo da empresa).
     */
    @PostMapping("/report/pdf")
    public ResponseEntity<byte[]> exportReportPdf(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody SaleFilterRequest filter) {
        byte[] content = saleReportService.generatePdfReport(tenantId, filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-vendas.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<SaleResponse> cancel(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(saleService.cancel(id, reason));
    }
}
