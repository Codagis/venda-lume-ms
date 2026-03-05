package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.sale.SaleFilterRequest;
import com.vendalume.vendalume.api.dto.sale.SaleResponse;
import com.vendalume.vendalume.api.dto.sale.SaleSummaryResponse;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.repository.TenantRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.vendalume.vendalume.domain.enums.PaymentMethod;
import com.vendalume.vendalume.domain.enums.SaleType;

/**
 * Serviço para geração de relatórios de vendas em Excel (XLS) e PDF.
 */
@Service
@RequiredArgsConstructor
public class SaleReportService {

    private static final DecimalFormatSymbols PT_BR = new DecimalFormatSymbols(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SaleService saleService;
    private final TenantRepository tenantRepository;
    private final TemplateEngine templateEngine;

    public byte[] generateExcelReport(UUID requestTenantId, SaleFilterRequest filter) {
        List<SaleResponse> sales = saleService.getSalesForReport(requestTenantId, filter);
        SaleSummaryResponse summary = saleService.getSummary(requestTenantId, filter);
        Tenant tenant = null;
        if (!sales.isEmpty()) {
            tenant = tenantRepository.findById(sales.get(0).getTenantId()).orElse(null);
        } else if (filter.getTenantId() != null) {
            tenant = tenantRepository.findById(filter.getTenantId()).orElse(null);
        }

        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet("Relatório de Vendas");

            HSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            HSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            HSSFCellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            int rowNum = 0;

            // Cabeçalho
            Row headerRow = sheet.createRow(rowNum++);
            String empresa = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                    ? tenant.getTradeName() : (tenant != null ? tenant.getName() : "VendaLume");
            createCell(headerRow, 0, "Relatório de Vendas - " + empresa, headerStyle);
            rowNum++;

            Row periodRow = sheet.createRow(rowNum++);
            createCell(periodRow, 0, "Período: " + formatPeriod(filter));
            rowNum++;

            Row summaryRow = sheet.createRow(rowNum++);
            createCell(summaryRow, 0, "Total de vendas: " + (summary != null ? summary.getCount() : 0));
            createCell(summaryRow, 1, "Valor total: R$ " + formatDecimal(summary != null ? summary.getTotalAmount() : BigDecimal.ZERO));
            rowNum++;

            rowNum++; // linha em branco

            // Cabeçalho da tabela
            Row tableHeader = sheet.createRow(rowNum++);
            String[] headers = {"Número", "Data", "Cliente", "Status", "Tipo", "Pagamento", "Subtotal", "Desconto", "Total"};
            for (int i = 0; i < headers.length; i++) {
                createCell(tableHeader, i, headers[i], headerStyle);
            }

            for (SaleResponse s : sales) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, s.getSaleNumber());
                createCell(row, 1, s.getSaleDate() != null ? s.getSaleDate().format(DATE_TIME_FMT) : "");
                createCell(row, 2, s.getCustomerName() != null ? s.getCustomerName() : "");
                createCell(row, 3, s.getStatus() != null ? s.getStatus().getDescription() : "");
                createCell(row, 4, s.getSaleType() != null ? s.getSaleType().getDescription() : "");
                createCell(row, 5, s.getPaymentMethod() != null ? s.getPaymentMethod().getDescription() : "");
                createCell(row, 6, s.getSubtotal(), numberStyle);
                createCell(row, 7, s.getDiscountAmount() != null ? s.getDiscountAmount() : BigDecimal.ZERO, numberStyle);
                createCell(row, 8, s.getTotal(), numberStyle);
            }

            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório Excel: " + e.getMessage());
        }
    }

    public byte[] generatePdfReport(UUID requestTenantId, SaleFilterRequest filter) {
        List<SaleResponse> sales = saleService.getSalesForReport(requestTenantId, filter);
        SaleSummaryResponse summary = saleService.getSummary(requestTenantId, filter);
        Tenant tenant = null;
        if (!sales.isEmpty()) {
            tenant = tenantRepository.findById(sales.get(0).getTenantId()).orElse(null);
        } else if (filter.getTenantId() != null) {
            tenant = tenantRepository.findById(filter.getTenantId()).orElse(null);
        }

        String nomeEmpresa = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                ? tenant.getTradeName() : (tenant != null ? tenant.getName() : "VendaLume");
        String logoUrl = tenant != null ? tenant.getLogoUrl() : null;
        String periodo = formatPeriod(filter);
        String dataGeracao = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
        long totalVendas = summary != null ? summary.getCount() : 0;
        String valorTotal = formatDecimal(summary != null ? summary.getTotalAmount() : BigDecimal.ZERO);
        String subtotalGeral = formatDecimal(summary != null ? summary.getSubtotalAmount() : BigDecimal.ZERO);
        String descontos = formatDecimal(summary != null ? summary.getDiscountAmount() : BigDecimal.ZERO);
        String taxas = formatDecimal(summary != null ? summary.getTaxAmount() : BigDecimal.ZERO);
        String fretes = formatDecimal(summary != null ? summary.getDeliveryFeeAmount() : BigDecimal.ZERO);

        // Totais por tipo de venda
        Map<String, SummaryItem> porTipoMap = new LinkedHashMap<>();
        for (SaleType st : SaleType.values()) {
            porTipoMap.put(st.name(), new SummaryItem(st.getDescription(), 0, BigDecimal.ZERO));
        }
        for (SaleResponse s : sales) {
            if (s.getSaleType() != null) {
                SummaryItem item = porTipoMap.get(s.getSaleType().name());
                if (item != null) {
                    item.count++;
                    item.valor = item.valor.add(s.getTotal() != null ? s.getTotal() : BigDecimal.ZERO);
                }
            }
        }
        porTipoMap.values().forEach(i -> i.valorFormatado = formatDecimal(i.valor));
        List<SummaryItem> porTipo = new ArrayList<>(porTipoMap.values());

        // Totais por forma de pagamento (segmento)
        Map<String, SummaryItem> porSegmentoMap = new LinkedHashMap<>();
        for (PaymentMethod pm : PaymentMethod.values()) {
            porSegmentoMap.put(pm.name(), new SummaryItem(pm.getDescription(), 0, BigDecimal.ZERO));
        }
        for (SaleResponse s : sales) {
            if (s.getPaymentMethod() != null) {
                SummaryItem item = porSegmentoMap.get(s.getPaymentMethod().name());
                if (item != null) {
                    item.count++;
                    item.valor = item.valor.add(s.getTotal() != null ? s.getTotal() : BigDecimal.ZERO);
                }
            }
        }
        porSegmentoMap.values().forEach(i -> i.valorFormatado = formatDecimal(i.valor));
        List<SummaryItem> porSegmento = new ArrayList<>(porSegmentoMap.values());

        List<SaleReportRow> rows = sales.stream().map(s -> SaleReportRow.builder()
                .numero(s.getSaleNumber())
                .data(s.getSaleDate() != null ? s.getSaleDate().format(DATE_TIME_FMT) : "—")
                .cliente(s.getCustomerName() != null ? s.getCustomerName() : "—")
                .status(s.getStatus() != null ? s.getStatus().getDescription() : "—")
                .tipo(s.getSaleType() != null ? s.getSaleType().getDescription() : "—")
                .pagamento(s.getPaymentMethod() != null ? s.getPaymentMethod().getDescription() : "—")
                .subtotal(formatDecimal(s.getSubtotal()))
                .desconto(formatDecimal(s.getDiscountAmount() != null ? s.getDiscountAmount() : BigDecimal.ZERO))
                .total(formatDecimal(s.getTotal()))
                .build()).toList();

        Context context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariable("nomeEmpresa", nomeEmpresa);
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("periodo", periodo);
        context.setVariable("dataGeracao", dataGeracao);
        context.setVariable("totalVendas", totalVendas);
        context.setVariable("valorTotal", valorTotal);
        context.setVariable("subtotalGeral", subtotalGeral);
        context.setVariable("descontos", descontos);
        context.setVariable("taxas", taxas);
        context.setVariable("fretes", fretes);
        context.setVariable("porTipo", porTipo);
        context.setVariable("porSegmento", porSegmento);
        context.setVariable("rows", rows);

        String html = templateEngine.process("report/sales-report", context);

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
            throw new RuntimeException("Erro ao gerar relatório PDF: " + e.getMessage());
        }
    }

    private String formatPeriod(SaleFilterRequest filter) {
        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            return filter.getStartDate().format(DATE_FMT) + " a " + filter.getEndDate().format(DATE_FMT);
        }
        if (filter.getStartDate() != null) {
            return "A partir de " + filter.getStartDate().format(DATE_FMT);
        }
        if (filter.getEndDate() != null) {
            return "Até " + filter.getEndDate().format(DATE_FMT);
        }
        return "Todos os períodos";
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) return "0,00";
        return new DecimalFormat("#,##0.00", PT_BR).format(value);
    }

    private void createCell(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
    }

    private void createCell(Row row, int col, String value, HSSFCellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int col, BigDecimal value, HSSFCellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value.doubleValue() : 0);
        cell.setCellStyle(style);
    }

    @lombok.Data
    public static class SummaryItem {
        public String label;
        public int count;
        public BigDecimal valor;
        public String valorFormatado;
        public SummaryItem(String label, int count, BigDecimal valor) {
            this.label = label;
            this.count = count;
            this.valor = valor != null ? valor : BigDecimal.ZERO;
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class SaleReportRow {
        private String numero;
        private String data;
        private String cliente;
        private String status;
        private String tipo;
        private String pagamento;
        private String subtotal;
        private String desconto;
        private String total;
    }
}
