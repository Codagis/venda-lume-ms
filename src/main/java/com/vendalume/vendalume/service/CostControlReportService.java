package com.vendalume.vendalume.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vendalume.vendalume.api.dto.costcontrol.*;
import com.vendalume.vendalume.api.dto.payroll.PayrollCalculationDto;
import com.vendalume.vendalume.api.dto.payroll.PayrollReportItemDto;
import com.vendalume.vendalume.domain.entity.Employee;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.domain.enums.AccountStatus;
import com.vendalume.vendalume.domain.enums.PaymentMethod;
import com.vendalume.vendalume.repository.EmployeeRepository;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.security.SecurityUtils;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Serviço para geração de relatórios de contas a pagar e contas a receber em Excel e PDF.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class CostControlReportService {

    private static final DecimalFormatSymbols PT_BR = new DecimalFormatSymbols(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] MESES_PT = { "janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro" };

    private final CostControlService costControlService;
    private final PayrollService payrollService;
    private final PayrollCalculationService payrollCalculationService;
    private final TenantRepository tenantRepository;
    private final EmployeeRepository employeeRepository;
    private final TemplateEngine templateEngine;

    public byte[] generatePaymentReceiptPdf(UUID payableId) {
        AccountPayableResponse ap = costControlService.findPayableById(payableId);
        Tenant tenant = tenantRepository.findById(ap.getTenantId()).orElse(null);
        String nomeEmpresa = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                ? tenant.getTradeName() : (tenant != null ? tenant.getName() : "VendaLume");
        String logoUrl = tenant != null ? tenant.getLogoUrl() : null;
        String beneficiario = ap.getEmployeeName() != null ? ap.getEmployeeName() : (ap.getSupplierName() != null ? ap.getSupplierName() : "—");
        String dataEmissao = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").format(Instant.now().atZone(ZoneId.systemDefault()));
        String dataVencimento = ap.getDueDate() != null ? ap.getDueDate().format(DATE_FMT) : "—";
        String dataPagamento = ap.getPaymentDate() != null ? ap.getPaymentDate().format(DATE_FMT) : "—";
        String formaPagamento = ap.getPaymentMethod() != null ? ap.getPaymentMethod().getDescription() : "—";
        String statusDesc = ap.getStatus() != null ? ap.getStatus().getDescription() : "—";
        String statusClass = ap.getStatus() != null ? "status-" + ap.getStatus().name() : "";
        String valorPago = formatDecimal(ap.getPaidAmount());
        String valorTotal = formatDecimal(ap.getAmount());
        String referencia = ap.getPayrollReference() != null ? ap.getPayrollReference() : "";

        Context context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariable("nomeEmpresa", nomeEmpresa);
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("dataEmissao", dataEmissao);
        context.setVariable("descricao", ap.getDescription() != null ? ap.getDescription() : "—");
        context.setVariable("beneficiario", beneficiario);
        context.setVariable("dataVencimento", dataVencimento);
        context.setVariable("dataPagamento", dataPagamento);
        context.setVariable("formaPagamento", formaPagamento);
        context.setVariable("statusDesc", statusDesc);
        context.setVariable("statusClass", statusClass);
        context.setVariable("valorPago", valorPago);
        context.setVariable("valorTotal", valorTotal);
        context.setVariable("referencia", referencia);

        String html = templateEngine.process("receipt/payment-receipt", context);
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
            throw new RuntimeException("Erro ao gerar comprovante: " + e.getMessage());
        }
    }

    /**
     * Gera o Recibo de Pagamento de Salário profissional em PDF para um funcionário e mês/ano.
     */
    public byte[] generateSalaryReceiptPdf(UUID tenantId, UUID employeeId, int year, int month) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada."));
        Employee emp = employeeRepository.findByIdAndTenantId(employeeId, tenantId).orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));

        String empregadorNome = tenant.getTradeName() != null && !tenant.getTradeName().isBlank() ? tenant.getTradeName() : tenant.getName();
        String empregadorEndereco = buildTenantAddress(tenant);
        String empregadorCnpj = tenant.getDocument() != null ? tenant.getDocument() : "—";

        int mesIdx = Math.max(0, Math.min(month - 1, 11));
        String mesAnoReferencia = capitalize(MESES_PT[mesIdx]) + "/" + year;

        String funcionarioCodigo = String.format("%04d", Math.abs(emp.getId().hashCode() % 10000));
        String funcionarioNome = emp.getName() != null ? emp.getName() : "—";
        String funcionarioCbo = emp.getCbo() != null && !emp.getCbo().isBlank() ? emp.getCbo() : "—";
        String funcionarioFuncao = emp.getRole() != null ? emp.getRole() : "—";

        PayrollCalculationDto calc = payrollCalculationService.calculate(emp);
        BigDecimal salary = calc.getSalary() != null ? calc.getSalary() : BigDecimal.ZERO;
        BigDecimal periculosidade = calc.getPericulosidade() != null ? calc.getPericulosidade() : BigDecimal.ZERO;
        BigDecimal overtimeVal = calc.getOvertimeVal() != null ? calc.getOvertimeVal() : BigDecimal.ZERO;
        BigDecimal dsrVal = calc.getDsrVal() != null ? calc.getDsrVal() : BigDecimal.ZERO;
        BigDecimal healthDed = calc.getHealthDed() != null ? calc.getHealthDed() : BigDecimal.ZERO;
        BigDecimal inssVal = calc.getInssVal() != null ? calc.getInssVal() : BigDecimal.ZERO;
        BigDecimal irrfVal = calc.getIrrfVal() != null ? calc.getIrrfVal() : BigDecimal.ZERO;
        BigDecimal totalProventosVal = calc.getTotalProventos() != null ? calc.getTotalProventos() : salary;
        BigDecimal totalDescontosVal = calc.getTotalDescontos() != null ? calc.getTotalDescontos() : BigDecimal.ZERO;
        BigDecimal liquidoVal = calc.getLiquido() != null ? calc.getLiquido() : totalProventosVal.subtract(totalDescontosVal);
        BigDecimal fgtsMesVal = totalProventosVal.multiply(new BigDecimal("0.08"));

        List<SalaryReceiptRow> linhas = new ArrayList<>();
        linhas.add(new SalaryReceiptRow("", "SALÁRIO BASE", mesAnoReferencia, formatDecimal(salary), ""));
        if (periculosidade.compareTo(BigDecimal.ZERO) > 0) {
            String refPeric = emp.getHazardousPayPercent() != null ? formatDecimal(emp.getHazardousPayPercent()) + "%" : "30%";
            linhas.add(new SalaryReceiptRow("", "ADICIONAL DE PERICULOSIDADE", refPeric, formatDecimal(periculosidade), ""));
        }
        if (overtimeVal.compareTo(BigDecimal.ZERO) > 0) {
            String refHe = emp.getOvertimeHours() != null ? emp.getOvertimeHours().stripTrailingZeros().toPlainString() : "—";
            linhas.add(new SalaryReceiptRow("", "HORAS EXTRAORDINÁRIAS (50%)", refHe, formatDecimal(overtimeVal), ""));
        }
        if (dsrVal.compareTo(BigDecimal.ZERO) > 0) {
            linhas.add(new SalaryReceiptRow("", "DESCANSO SEMANAL REMUNERADO", "", formatDecimal(dsrVal), ""));
        }
        if (healthDed.compareTo(BigDecimal.ZERO) > 0) {
            linhas.add(new SalaryReceiptRow("", "PLANO DE SAÚDE", "", "", formatDecimal(healthDed)));
        }
        if (inssVal.compareTo(BigDecimal.ZERO) > 0) {
            String refInss = emp.getInssPercent() != null ? formatDecimal(emp.getInssPercent()) + "%" : "";
            linhas.add(new SalaryReceiptRow("", "INSS", refInss, "", formatDecimal(inssVal)));
        }
        if (irrfVal.compareTo(BigDecimal.ZERO) > 0) {
            linhas.add(new SalaryReceiptRow("", "IRRF", "", "", formatDecimal(irrfVal)));
        }

        Context context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariable("empregadorNome", empregadorNome);
        context.setVariable("empregadorEndereco", empregadorEndereco);
        context.setVariable("empregadorCnpj", empregadorCnpj);
        context.setVariable("mesAnoReferencia", mesAnoReferencia);
        context.setVariable("funcionarioCodigo", funcionarioCodigo);
        context.setVariable("funcionarioNome", funcionarioNome);
        context.setVariable("funcionarioCbo", funcionarioCbo);
        context.setVariable("funcionarioFuncao", funcionarioFuncao);
        context.setVariable("linhas", linhas);
        context.setVariable("totalProventos", formatDecimal(totalProventosVal));
        context.setVariable("totalDescontos", formatDecimal(totalDescontosVal));
        context.setVariable("liquidoReceber", formatDecimal(liquidoVal));
        context.setVariable("salarioBase", formatDecimal(salary));
        context.setVariable("baseCalcInssFgts", formatDecimal(totalProventosVal));
        context.setVariable("fgtsMes", formatDecimal(fgtsMesVal));

        String html = templateEngine.process("receipt/payroll-salary-receipt", context);
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
            throw new RuntimeException("Erro ao gerar recibo de salário: " + e.getMessage());
        }
    }

    private String buildTenantAddress(Tenant t) {
        if (t.getAddressStreet() == null && t.getAddressCity() == null) return "—";
        StringBuilder sb = new StringBuilder();
        if (t.getAddressStreet() != null) sb.append(t.getAddressStreet());
        if (t.getAddressNumber() != null && !t.getAddressNumber().isBlank()) sb.append(", ").append(t.getAddressNumber());
        if (t.getAddressNeighborhood() != null && !t.getAddressNeighborhood().isBlank()) sb.append(" - ").append(t.getAddressNeighborhood());
        if (t.getAddressCity() != null && !t.getAddressCity().isBlank()) sb.append(" - ").append(t.getAddressCity());
        if (t.getAddressState() != null && !t.getAddressState().isBlank()) sb.append("/").append(t.getAddressState());
        if (t.getAddressZip() != null && !t.getAddressZip().isBlank()) sb.append(" - CEP ").append(t.getAddressZip());
        return sb.length() > 0 ? sb.toString() : "—";
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class SalaryReceiptRow {
        private String codigo;
        private String descricao;
        private String referencia;
        private String proventos;
        private String descontos;
    }

    public byte[] generatePayrollPdf(UUID requestTenantId, int year, int month) {
        UUID tenantId;
        if (requestTenantId != null) {
            tenantId = requestTenantId;
        } else {
            if (SecurityUtils.isCurrentUserRoot()) {
                throw new IllegalArgumentException("Selecione a empresa para gerar a folha.");
            }
            tenantId = SecurityUtils.requireTenantId();
        }
        Tenant tenant = tenantId != null ? tenantRepository.findById(tenantId).orElse(null) : null;
        List<Employee> employees = tenantId != null ? employeeRepository.findByTenantIdAndActiveTrueOrderByName(tenantId) : List.of();

        String nomeEmpresa = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                ? tenant.getTradeName() : (tenant != null ? tenant.getName() : "VendaLume");
        String empresaCnpj = tenant != null && tenant.getDocument() != null ? tenant.getDocument() : "—";
        String empresaEndereco = tenant != null ? buildTenantAddress(tenant) : "—";
        int mesIdx = Math.max(0, Math.min(month - 1, 11));
        String competencia = capitalize(MESES_PT[mesIdx]) + " / " + year;
        String dataEmissao = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(Instant.now().atZone(ZoneId.systemDefault()));

        List<FolhaRow> rows = new ArrayList<>();
        BigDecimal totalSalarios = BigDecimal.ZERO;
        BigDecimal totalHorasExtras = BigDecimal.ZERO;
        BigDecimal totalAdicional = BigDecimal.ZERO;
        BigDecimal totalProventos = BigDecimal.ZERO;
        BigDecimal totalInss = BigDecimal.ZERO;
        BigDecimal totalIrrf = BigDecimal.ZERO;
        BigDecimal totalDescontos = BigDecimal.ZERO;
        BigDecimal totalLiquido = BigDecimal.ZERO;

        for (Employee emp : employees) {
            PayrollCalculationDto calc = payrollCalculationService.calculate(emp);
            BigDecimal salary = calc.getSalary() != null ? calc.getSalary() : BigDecimal.ZERO;
            BigDecimal periculosidade = calc.getPericulosidade() != null ? calc.getPericulosidade() : BigDecimal.ZERO;
            BigDecimal overtimeVal = calc.getOvertimeVal() != null ? calc.getOvertimeVal() : BigDecimal.ZERO;
            BigDecimal inssVal = calc.getInssVal() != null ? calc.getInssVal() : BigDecimal.ZERO;
            BigDecimal irrfVal = calc.getIrrfVal() != null ? calc.getIrrfVal() : BigDecimal.ZERO;
            BigDecimal proventos = calc.getTotalProventos() != null ? calc.getTotalProventos() : BigDecimal.ZERO;
            BigDecimal descontos = calc.getTotalDescontos() != null ? calc.getTotalDescontos() : BigDecimal.ZERO;
            BigDecimal liquido = calc.getLiquido() != null ? calc.getLiquido() : BigDecimal.ZERO;

            totalSalarios = totalSalarios.add(salary);
            totalHorasExtras = totalHorasExtras.add(overtimeVal);
            totalAdicional = totalAdicional.add(periculosidade);
            totalProventos = totalProventos.add(proventos);
            totalInss = totalInss.add(inssVal);
            totalIrrf = totalIrrf.add(irrfVal);
            totalDescontos = totalDescontos.add(descontos);
            totalLiquido = totalLiquido.add(liquido);

            String cod = String.format("%04d", Math.abs(emp.getId().hashCode() % 10000));
            rows.add(new FolhaRow(
                    cod,
                    emp.getName() != null ? emp.getName() : "—",
                    emp.getRole() != null ? emp.getRole() : "—",
                    formatDecimal(salary),
                    formatDecimal(overtimeVal),
                    formatDecimal(periculosidade),
                    formatDecimal(proventos),
                    formatDecimal(inssVal),
                    formatDecimal(irrfVal),
                    formatDecimal(descontos),
                    formatDecimal(liquido)
            ));
        }

        BigDecimal totalFgts = totalProventos.multiply(new BigDecimal("0.08"));

        Context context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariable("nomeEmpresa", nomeEmpresa);
        context.setVariable("empresaCnpj", empresaCnpj);
        context.setVariable("empresaEndereco", empresaEndereco);
        context.setVariable("competencia", competencia);
        context.setVariable("dataEmissao", dataEmissao);
        context.setVariable("totalFuncionarios", rows.size());
        context.setVariable("rows", rows);
        context.setVariable("totalSalarios", formatDecimal(totalSalarios));
        context.setVariable("totalHorasExtras", formatDecimal(totalHorasExtras));
        context.setVariable("totalAdicional", formatDecimal(totalAdicional));
        context.setVariable("totalProventos", formatDecimal(totalProventos));
        context.setVariable("totalInss", formatDecimal(totalInss));
        context.setVariable("totalIrrf", formatDecimal(totalIrrf));
        context.setVariable("totalDescontos", formatDecimal(totalDescontos));
        context.setVariable("totalLiquido", formatDecimal(totalLiquido));
        context.setVariable("totalFgts", formatDecimal(totalFgts));

        String html = templateEngine.process("report/payroll-report", context);
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
            throw new RuntimeException("Erro ao gerar PDF da folha: " + e.getMessage());
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class FolhaRow {
        private String cod;
        private String funcionario;
        private String funcao;
        private String salarioBase;
        private String horasExtras;
        private String adicional;
        private String totalProventos;
        private String inss;
        private String irrf;
        private String totalDescontos;
        private String liquido;
    }

    public byte[] generatePayrollExcel(UUID requestTenantId, int year, int month) {
        List<PayrollReportItemDto> items = payrollService.getPayrollReport(requestTenantId, year, month);
        Tenant tenant = requestTenantId != null ? tenantRepository.findById(requestTenantId).orElse(null) : null;
        List<String> headers = List.of("Funcionário", "CPF/CNPJ", "Função", "Salário", "Dia venc.", "Vencimento", "Pago", "Status");
        List<Object[]> rows = items.stream().map(i -> new Object[]{
                i.getEmployeeName(),
                i.getDocument(),
                i.getRole(),
                i.getSalary(),
                i.getPaymentDay(),
                i.getDueDate() != null ? i.getDueDate().format(DATE_FMT) : "",
                i.getPaidAmount(),
                i.getStatus() != null ? i.getStatus().getDescription() : "—"
        }).toList();
        return buildExcel(tenant, "Folha de Pagamento " + year + "/" + String.format("%02d", month), headers, rows);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PayrollReportRow {
        private String employeeName;
        private String document;
        private String role;
        private String salary;
        private String dueDate;
        private String paidAmount;
        private String status;
    }

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

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.forLanguageTag("pt-BR")) + s.substring(1);
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
