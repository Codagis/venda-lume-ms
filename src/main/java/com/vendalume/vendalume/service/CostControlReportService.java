package com.vendalume.vendalume.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vendalume.vendalume.api.dto.costcontrol.*;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.domain.enums.AccountStatus;
import com.vendalume.vendalume.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Serviço para geração de relatórios de contas a pagar e contas a receber em Excel e PDF.
 */
@Service
@RequiredArgsConstructor
public class CostControlReportService {

    private static final DecimalFormatSymbols PT_BR = new DecimalFormatSymbols(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CostControlService costControlService;
    private final TenantRepository tenantRepository;
    private final TemplateEngine templateEngine;

    // --- Contas a Pagar ---

    public byte[] generatePayablesExcel(UUID requestTenantId, AccountPayableFilterRequest filter) {
        List<AccountPayableResponse> list = costControlService.getPayablesForReport(requestTenantId, filter);
        Tenant tenant = resolveTenant(list, filter.getTenantId());
        return buildExcel(
                tenant,
                "Contas a Pagar",
                List.of("Fornecedor", "Descrição", "Ref.", "Categoria", "Vencimento", "Valor", "Pago", "Pendente", "Status"),
                list.stream().map(p -> new Object[]{
                        p.getSupplierName(),
                        p.getDescription(),
                        p.getReference(),
                        p.getCategory(),
                        p.getDueDate() != null ? p.getDueDate().format(DATE_FMT) : "",
                        p.getAmount(),
                        p.getPaidAmount(),
                        pendingAmount(p.getAmount(), p.getPaidAmount()),
                        p.getStatus() != null ? p.getStatus().getDescription() : ""
                }).toList()
        );
    }

    public byte[] generatePayablesPdf(UUID requestTenantId, AccountPayableFilterRequest filter) {
        List<AccountPayableResponse> list = costControlService.getPayablesForReport(requestTenantId, filter);
        Tenant tenant = resolveTenant(list, filter.getTenantId());
        return buildPdf(tenant, "Contas a Pagar", "payables", list.stream()
                .map(p -> new ReportRow(
                        p.getSupplierName(),
                        p.getDescription(),
                        p.getDueDate() != null ? p.getDueDate().format(DATE_FMT) : "—",
                        formatDecimal(p.getAmount()),
                        formatDecimal(p.getPaidAmount()),
                        formatDecimal(pendingAmount(p.getAmount(), p.getPaidAmount())),
                        p.getStatus() != null ? p.getStatus().getDescription() : "—"
                )).toList(),
                list.stream().map(AccountPayableResponse::getAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add),
                list.stream().map(AccountPayableResponse::getPaidAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add)
        );
    }

    // --- Contas a Receber ---

    public byte[] generateReceivablesExcel(UUID requestTenantId, AccountReceivableFilterRequest filter) {
        List<AccountReceivableResponse> list = costControlService.getReceivablesForReport(requestTenantId, filter);
        Tenant tenant = resolveTenantFromReceivables(list, filter.getTenantId());
        return buildExcel(
                tenant,
                "Contas a Receber",
                List.of("Cliente", "Descrição", "Ref.", "Categoria", "Vencimento", "Valor", "Recebido", "Pendente", "Status"),
                list.stream().map(r -> new Object[]{
                        r.getCustomerName(),
                        r.getDescription(),
                        r.getReference(),
                        r.getCategory(),
                        r.getDueDate() != null ? r.getDueDate().format(DATE_FMT) : "",
                        r.getAmount(),
                        r.getReceivedAmount(),
                        pendingAmount(r.getAmount(), r.getReceivedAmount()),
                        r.getStatus() != null ? r.getStatus().getDescription() : ""
                }).toList()
        );
    }

    public byte[] generateReceivablesPdf(UUID requestTenantId, AccountReceivableFilterRequest filter) {
        List<AccountReceivableResponse> list = costControlService.getReceivablesForReport(requestTenantId, filter);
        Tenant tenant = resolveTenantFromReceivables(list, filter.getTenantId());
        return buildPdf(tenant, "Contas a Receber", "receivables", list.stream()
                .map(r -> new ReportRow(
                        r.getCustomerName(),
                        r.getDescription(),
                        r.getDueDate() != null ? r.getDueDate().format(DATE_FMT) : "—",
                        formatDecimal(r.getAmount()),
                        formatDecimal(r.getReceivedAmount()),
                        formatDecimal(pendingAmount(r.getAmount(), r.getReceivedAmount())),
                        r.getStatus() != null ? r.getStatus().getDescription() : "—"
                )).toList(),
                list.stream().map(AccountReceivableResponse::getAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add),
                list.stream().map(AccountReceivableResponse::getReceivedAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add)
        );
    }

    private Tenant resolveTenant(List<AccountPayableResponse> list, UUID filterTenantId) {
        if (!list.isEmpty()) {
            return tenantRepository.findById(list.get(0).getTenantId()).orElse(null);
        }
        return filterTenantId != null ? tenantRepository.findById(filterTenantId).orElse(null) : null;
    }

    private Tenant resolveTenantFromReceivables(List<AccountReceivableResponse> list, UUID filterTenantId) {
        if (!list.isEmpty()) {
            return tenantRepository.findById(list.get(0).getTenantId()).orElse(null);
        }
        return filterTenantId != null ? tenantRepository.findById(filterTenantId).orElse(null) : null;
    }

    private BigDecimal pendingAmount(BigDecimal total, BigDecimal paid) {
        if (total == null) return BigDecimal.ZERO;
        BigDecimal p = paid != null ? paid : BigDecimal.ZERO;
        return total.subtract(p).max(BigDecimal.ZERO);
    }

    private String formatDecimal(BigDecimal v) {
        if (v == null) return "0,00";
        return new DecimalFormat("#,##0.00", PT_BR).format(v);
    }

    private byte[] buildExcel(Tenant tenant, String reportTitle, List<String> headers, List<Object[]> rows) {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet(reportTitle);
            HSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            HSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            HSSFCellStyle numberStyle = wb.createCellStyle();
            numberStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));

            int rowNum = 0;
            String empresa = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                    ? tenant.getTradeName() : (tenant != null ? tenant.getName() : "VendaLume");
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue(reportTitle + " - " + empresa);
            rowNum++;
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.size(); i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers.get(i));
                c.setCellStyle(headerStyle);
            }
            for (Object[] row : rows) {
                Row r = sheet.createRow(rowNum++);
                for (int i = 0; i < row.length; i++) {
                    Cell c = r.createCell(i);
                    if (row[i] instanceof BigDecimal) {
                        c.setCellValue(((BigDecimal) row[i]).doubleValue());
                        c.setCellStyle(numberStyle);
                    } else {
                        c.setCellValue(row[i] != null ? row[i].toString() : "");
                    }
                }
            }
            for (int i = 0; i < headers.size(); i++) sheet.autoSizeColumn(i);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                wb.write(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Excel: " + e.getMessage());
        }
    }

    private byte[] buildPdf(Tenant tenant, String reportTitle, String templateName, List<ReportRow> rows, BigDecimal totalAmount, BigDecimal totalPaidReceived) {
        String nomeEmpresa = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                ? tenant.getTradeName() : (tenant != null ? tenant.getName() : "VendaLume");
        String logoUrl = tenant != null ? tenant.getLogoUrl() : null;
        String dataGeracao = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
        BigDecimal paid = totalPaidReceived != null ? totalPaidReceived : BigDecimal.ZERO;
        BigDecimal total = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        String totalGeral = formatDecimal(total);
        String totalPagoRecebido = formatDecimal(paid);
        String totalPendente = formatDecimal(total.subtract(paid));

        Context context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariable("nomeEmpresa", nomeEmpresa);
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("reportTitle", reportTitle);
        context.setVariable("dataGeracao", dataGeracao);
        context.setVariable("totalGeral", totalGeral);
        context.setVariable("totalPagoRecebido", totalPagoRecebido);
        context.setVariable("totalPendente", totalPendente);
        context.setVariable("rows", rows);

        String html = templateEngine.process("report/cost-control-report", context);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);
            var w3cDoc = new W3CDom().fromJsoup(Jsoup.parse(html, "UTF-8"));
            builder.withW3cDocument(w3cDoc, "");
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ReportRow {
        private String partyName;
        private String description;
        private String dueDate;
        private String amount;
        private String paidReceived;
        private String pending;
        private String status;
    }
}
