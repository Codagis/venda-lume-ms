package com.vendalume.vendalume.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vendalume.vendalume.api.dto.sale.SaleItemResponse;
import com.vendalume.vendalume.api.dto.sale.SaleResponse;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.service.dto.ReceiptItemDto;
import com.vendalume.vendalume.service.dto.ReceiptPdfContext;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Serviço para geração do PDF do cupom fiscal da venda.
 * Utiliza Thymeleaf para o layout e OpenHTML to PDF para renderização.
 */
@Service
@RequiredArgsConstructor
public class SaleReceiptPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DecimalFormatSymbols PT_BR = new DecimalFormatSymbols(new Locale("pt", "BR"));

    @Value("${app.version:01.00.00}")
    private String appVersion;

    private final SaleService saleService;
    private final TenantRepository tenantRepository;
    private final TemplateEngine templateEngine;

    public byte[] generateReceiptPdf(UUID saleId) {
        SaleResponse sale = saleService.getById(saleId);
        if (!Boolean.TRUE.equals(sale.getCanEmitFiscalReceipt())) {
            throw new IllegalArgumentException("Nenhum produto desta venda está configurado para NFC-e ou empresa sem configuração fiscal.");
        }
        Tenant tenant = tenantRepository.findById(sale.getTenantId()).orElse(null);
        boolean ieOk = tenant.getStateRegistration() != null && !tenant.getStateRegistration().isBlank();
        boolean imOk = tenant.getMunicipalRegistration() != null && !tenant.getMunicipalRegistration().isBlank();
        if (!ieOk && !imOk) {
            throw new IllegalArgumentException("Cupom fiscal requer IE ou IM preenchido.");
        }

        ReceiptPdfContext ctx = buildContext(sale, tenant);
        String html = renderTemplate(ctx);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useDefaultPageSize(100, 297, PdfRendererBuilder.PageSizeUnits.MM);

            org.w3c.dom.Document w3cDoc = htmlToW3cDocument(html);
            builder.withW3cDocument(w3cDoc, "");
            builder.toStream(baos);
            builder.run();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do cupom: " + e.getMessage());
        }
    }

    private ReceiptPdfContext buildContext(SaleResponse sale, Tenant tenant) {
        // Dados da empresa
        String razaoSocial = tenant != null ? (tenant.getName() != null ? tenant.getName() : "-") : "VendaLume";
        String nomeFantasia = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                ? tenant.getTradeName() : razaoSocial;
        String enderecoEmpresa = buildTenantAddress(tenant);
        String cnpj = tenant != null && tenant.getDocument() != null && !tenant.getDocument().isBlank()
                ? tenant.getDocument() : "-";
        String ie = tenant != null && tenant.getStateRegistration() != null && !tenant.getStateRegistration().isBlank()
                ? tenant.getStateRegistration() : "ISENTO";
        String im = tenant != null && tenant.getMunicipalRegistration() != null && !tenant.getMunicipalRegistration().isBlank()
                ? tenant.getMunicipalRegistration() : "ISENTO";

        // Dados do cupom
        String dataEmissao = sale.getSaleDate() != null ? sale.getSaleDate().format(DATE_FMT) : "-";
        String horaEmissao = sale.getSaleDate() != null ? sale.getSaleDate().format(TIME_FMT) : "-";
        String ccf = sale.getSaleNumber() != null ? padLeft(sale.getSaleNumber(), 6) : "000000";
        String coo = ccf;

        // Dados do consumidor
        String cpfCnpj = sale.getCustomerDocument();
        String nomeConsumidor = sale.getCustomerName();
        String enderecoConsumidor = sale.getDeliveryAddress();

        // Itens
        List<ReceiptItemDto> listaItens = new ArrayList<>();
        if (sale.getItems() != null) {
            for (SaleItemResponse item : sale.getItems()) {
                listaItens.add(ReceiptItemDto.builder()
                        .descricaoProduto(truncate(item.getProductName(), 50))
                        .codigoProduto(item.getProductSku() != null ? item.getProductSku() : "-")
                        .quantidadeProduto(item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO)
                        .valorUnitarioProduto(item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO)
                        .valorTotalProduto(item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO)
                        .build());
            }
        }

        // Totais
        BigDecimal subtotal = sale.getSubtotal() != null ? sale.getSubtotal() : BigDecimal.ZERO;
        BigDecimal desconto = sale.getDiscountAmount() != null ? sale.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = sale.getTotal() != null ? sale.getTotal() : BigDecimal.ZERO;

        // Pagamento
        String formaPagto = sale.getPaymentMethod() != null ? sale.getPaymentMethod().getDescription() : "-";
        String valorRecebido = sale.getAmountPaid() != null ? formatDecimal(sale.getAmountPaid()) : null;
        BigDecimal troco = sale.getChangeAmount();
        Integer qtdParcelas = sale.getInstallmentsCount();
        BigDecimal valorParcela = null;
        if (qtdParcelas != null && qtdParcelas > 0 && total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            valorParcela = total.divide(BigDecimal.valueOf(qtdParcelas), 2, java.math.RoundingMode.HALF_UP);
        }

        // MD5 e dados fiscais
        String hashMd5 = generateReceiptMd5(sale);
        String ecfSerie = generateSaleSerialNumber(sale, tenant);
        String ecfModelo = tenant != null && tenant.getEcfModel() != null && !tenant.getEcfModel().isBlank()
                ? tenant.getEcfModel() : "VendaLume PDV";
        String chaveNfce = sale.getInvoiceKey() != null && !sale.getInvoiceKey().isBlank()
                ? sale.getInvoiceKey() : hashMd5;

        return ReceiptPdfContext.builder()
                .razaoSocialEmpresa(razaoSocial)
                .nomeFantasiaEmpresa(nomeFantasia)
                .enderecoCompletoEmpresa(enderecoEmpresa)
                .cnpjEmpresa(cnpj)
                .inscricaoEstadualEmpresa(ie)
                .inscricaoMunicipalEmpresa(im)
                .dataEmissaoFormatada(dataEmissao)
                .horaEmissaoFormatada(horaEmissao)
                .contadorCupomFiscal(ccf)
                .contadorOrdemOperacao(coo)
                .cpfOuCnpjConsumidor(cpfCnpj)
                .nomeConsumidor(nomeConsumidor)
                .enderecoConsumidor(enderecoConsumidor)
                .listaItensVenda(listaItens)
                .valorSubtotalVenda(subtotal)
                .valorDescontoVenda(desconto)
                .valorTotalFinalVenda(total)
                .descricaoFormaPagamento(formaPagto)
                .valorRecebidoPagamento(valorRecebido)
                .valorTrocoPagamento(troco)
                .quantidadeParcelas(qtdParcelas)
                .valorParcela(valorParcela)
                .hashMd5CupomFiscal(hashMd5)
                .numeroSerieEquipamentoFiscal(ecfSerie)
                .modeloEquipamentoFiscal(ecfModelo)
                .versaoSoftwareAplicativoFiscal(appVersion)
                .chaveAcessoNfce(chaveNfce)
                .build();
    }

    private String buildTenantAddress(Tenant t) {
        if (t == null) return "-";
        List<String> parts = new ArrayList<>();
        if (t.getAddressStreet() != null && !t.getAddressStreet().isBlank()) {
            String street = t.getAddressStreet();
            if (t.getAddressNumber() != null && !t.getAddressNumber().isBlank()) street += ", " + t.getAddressNumber();
            parts.add(street);
        }
        if (t.getAddressComplement() != null && !t.getAddressComplement().isBlank()) parts.add(t.getAddressComplement());
        if (t.getAddressNeighborhood() != null && !t.getAddressNeighborhood().isBlank()) parts.add(t.getAddressNeighborhood());
        if (t.getAddressCity() != null || t.getAddressState() != null) {
            String cityState = (t.getAddressCity() != null ? t.getAddressCity() : "")
                    + (t.getAddressState() != null && !t.getAddressState().isBlank()
                    ? (t.getAddressCity() != null ? "/" : "") + t.getAddressState() : "");
            if (!cityState.isBlank()) parts.add(cityState);
        }
        if (t.getAddressZip() != null && !t.getAddressZip().isBlank()) parts.add("CEP: " + t.getAddressZip());
        return parts.isEmpty() ? "-" : String.join(" - ", parts);
    }

    private String generateReceiptMd5(SaleResponse sale) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(sale.getId()).append(sale.getSaleNumber()).append(sale.getSaleDate())
                    .append(sale.getTotal()).append(sale.getSubtotal());
            if (sale.getItems() != null) {
                sale.getItems().forEach(i -> sb.append(i.getProductName()).append(i.getQuantity()).append(i.getTotal()));
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return "00000000000000000000000000000000";
        }
    }

    private String generateSaleSerialNumber(SaleResponse sale, Tenant tenant) {
        if (tenant != null && tenant.getEcfSeries() != null && !tenant.getEcfSeries().isBlank()) {
            return tenant.getEcfSeries();
        }
        String num = sale.getSaleNumber() != null ? padLeft(sale.getSaleNumber(), 9) : "000000001";
        return "VSL" + num;
    }

    private String padLeft(String s, int len) {
        if (s == null) s = "";
        return s.length() >= len ? s.substring(s.length() - len) : String.format("%" + len + "s", s).replace(' ', '0');
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) return null;
        return new DecimalFormat("#,##0.00", PT_BR).format(value);
    }

    private String truncate(String s, int max) {
        if (s == null) return "-";
        return s.length() <= max ? s : s.substring(0, max);
    }

    private String renderTemplate(ReceiptPdfContext ctx) {
        Context context = new Context();
        context.setVariable("razaoSocialEmpresa", ctx.getRazaoSocialEmpresa());
        context.setVariable("nomeFantasiaEmpresa", ctx.getNomeFantasiaEmpresa());
        context.setVariable("enderecoCompletoEmpresa", ctx.getEnderecoCompletoEmpresa());
        context.setVariable("cnpjEmpresa", ctx.getCnpjEmpresa());
        context.setVariable("inscricaoEstadualEmpresa", ctx.getInscricaoEstadualEmpresa());
        context.setVariable("inscricaoMunicipalEmpresa", ctx.getInscricaoMunicipalEmpresa());
        context.setVariable("dataEmissaoFormatada", ctx.getDataEmissaoFormatada());
        context.setVariable("horaEmissaoFormatada", ctx.getHoraEmissaoFormatada());
        context.setVariable("contadorCupomFiscal", ctx.getContadorCupomFiscal());
        context.setVariable("contadorOrdemOperacao", ctx.getContadorOrdemOperacao());
        context.setVariable("cpfOuCnpjConsumidor", ctx.getCpfOuCnpjConsumidor());
        context.setVariable("nomeConsumidor", ctx.getNomeConsumidor());
        context.setVariable("enderecoConsumidor", ctx.getEnderecoConsumidor());
        context.setVariable("listaItensVenda", ctx.getListaItensVenda());
        context.setVariable("valorSubtotalVenda", ctx.getValorSubtotalVenda());
        context.setVariable("valorDescontoVenda", ctx.getValorDescontoVenda());
        context.setVariable("valorTotalFinalVenda", ctx.getValorTotalFinalVenda());
        context.setVariable("descricaoFormaPagamento", ctx.getDescricaoFormaPagamento());
        context.setVariable("valorRecebidoPagamento", ctx.getValorRecebidoPagamento());
        context.setVariable("valorTrocoPagamento", ctx.getValorTrocoPagamento());
        context.setVariable("quantidadeParcelas", ctx.getQuantidadeParcelas());
        context.setVariable("valorParcela", ctx.getValorParcela());
        context.setVariable("hashMd5CupomFiscal", ctx.getHashMd5CupomFiscal());
        context.setVariable("numeroSerieEquipamentoFiscal", ctx.getNumeroSerieEquipamentoFiscal());
        context.setVariable("modeloEquipamentoFiscal", ctx.getModeloEquipamentoFiscal());
        context.setVariable("versaoSoftwareAplicativoFiscal", ctx.getVersaoSoftwareAplicativoFiscal());
        context.setVariable("chaveAcessoNfce", ctx.getChaveAcessoNfce());

        return templateEngine.process("receipt/sale-receipt", context);
    }

    private org.w3c.dom.Document htmlToW3cDocument(String html) {
        var jsoupDoc = Jsoup.parse(html, "UTF-8");
        return new W3CDom().fromJsoup(jsoupDoc);
    }
}
