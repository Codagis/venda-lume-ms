package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.costcontrol.AccountPayableResponse;
import com.vendalume.vendalume.api.dto.payroll.GeneratePayrollBatchRequest;
import com.vendalume.vendalume.api.dto.payroll.GeneratedPayrollDto;
import com.vendalume.vendalume.api.dto.payroll.PayrollReportItemDto;
import com.vendalume.vendalume.security.SecurityUtils;
import com.vendalume.vendalume.service.CostControlReportService;
import com.vendalume.vendalume.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST que expõe os endpoints relacionados a PayrollController.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Tag(name = "Folha de Pagamento", description = "Geração de contas a pagar recorrentes e relatório de folha")
@RestController
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final CostControlReportService costControlReportService;

    @Operation(summary = "Gerar contas a pagar do mês para todos os funcionários ativos")
    @PostMapping("/generate")
    public ResponseEntity<List<AccountPayableResponse>> generateMonthly(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam int year,
            @RequestParam int month) {
        List<AccountPayableResponse> created = payrollService.generateMonthlyPayables(tenantId, year, month);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Gerar contas a pagar para funcionários e meses selecionados")
    @PostMapping("/generate-batch")
    public ResponseEntity<List<AccountPayableResponse>> generateBatch(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody GeneratePayrollBatchRequest request) {
        List<AccountPayableResponse> created = payrollService.generatePayablesBatch(tenantId, request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Listar folhas de pagamento já geradas (competências com contas a pagar de folha)")
    @GetMapping("/generated")
    public ResponseEntity<List<GeneratedPayrollDto>> listGenerated(
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(payrollService.listGeneratedPayrolls(tenantId));
    }

    @Operation(summary = "Relatório de folha de pagamento (consulta)")
    @GetMapping("/report")
    public ResponseEntity<List<PayrollReportItemDto>> getReport(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(payrollService.getPayrollReport(tenantId, year, month));
    }

    @Operation(summary = "Exportar folha de pagamento em PDF")
    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> exportPayrollPdf(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam int year,
            @RequestParam int month) {
        byte[] content = costControlReportService.generatePayrollPdf(tenantId, year, month);
        String filename = "folha-pagamento-" + year + "-" + String.format("%02d", month) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    @Operation(summary = "Exportar folha de pagamento em Excel")
    @GetMapping("/report/excel")
    public ResponseEntity<byte[]> exportPayrollExcel(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam int year,
            @RequestParam int month) {
        byte[] content = costControlReportService.generatePayrollExcel(tenantId, year, month);
        String filename = "folha-pagamento-" + year + "-" + String.format("%02d", month) + ".xls";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(content);
    }

    @Operation(summary = "Gerar Recibo de Pagamento de Salário (PDF) por funcionário e mês")
    @GetMapping("/receipt/{employeeId}")
    public ResponseEntity<byte[]> getSalaryReceiptPdf(
            @PathVariable UUID employeeId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) UUID tenantId) {
        UUID tenantIdResolved = tenantId != null ? tenantId : SecurityUtils.requireTenantId();
        byte[] content = costControlReportService.generateSalaryReceiptPdf(tenantIdResolved, employeeId, year, month);
        String filename = "recibo-salario-" + year + "-" + String.format("%02d", month) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }
}
