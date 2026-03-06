package com.vendalume.vendalume.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vendalume.vendalume.api.dto.sale.SaleItemResponse;
import com.vendalume.vendalume.api.dto.sale.SaleResponse;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.service.dto.SimpleReceiptItemDto;
import lombok.RequiredArgsConstructor;
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
 * Serviço para geração de cupom simplificado em PDF.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class SaleSimpleReceiptPdfService {

    private static final DecimalFormatSymbols PT_BR = new DecimalFormatSymbols(new Locale("pt", "BR"));

    private final SaleService saleService;
    private final TenantRepository tenantRepository;
    private final TemplateEngine templateEngine;

    public byte[] generateSimpleReceiptPdf(UUID saleId) {
        SaleResponse sale = saleService.getById(saleId);
        if (!Boolean.TRUE.equals(sale.getCanEmitSimpleReceipt())) {
            throw new IllegalArgumentException("Nenhum produto desta venda está configurado para comprovante simples.");
        }
        Tenant tenant = tenantRepository.findById(sale.getTenantId()).orElse(null);


        String nomeLoja = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                ? tenant.getTradeName() : (tenant != null ? tenant.getName() : "VendaLume");
        String nomeCliente = sale.getCustomerName() != null ? sale.getCustomerName() : "—";
        String nomeVendedor = sale.getSellerName() != null ? sale.getSellerName() : "—";
        String numeroVenda = sale.getSaleNumber() != null ? sale.getSaleNumber() : sale.getId() != null ? sale.getId().toString() : "—";

        List<SimpleReceiptItemDto> listaItens = new ArrayList<>();
        if (sale.getItems() != null) {
            for (SaleItemResponse item : sale.getItems()) {
                listaItens.add(SimpleReceiptItemDto.builder()
                        .descricaoProduto(truncate(item.getProductName(), 100))
                        .observacaoItem(item.getObservations())
                        .quantidadeProduto(item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO)
                        .valorUnitarioProduto(item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO)
                        .valorTotalProduto(item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO)
                        .build());
            }
        }

        BigDecimal subtotal = sale.getSubtotal() != null ? sale.getSubtotal() : BigDecimal.ZERO;
        BigDecimal desconto = sale.getDiscountAmount() != null ? sale.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal frete = sale.getDeliveryFee() != null ? sale.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal total = sale.getTotal() != null ? sale.getTotal() : BigDecimal.ZERO;

        String valorTotalProdutos = formatDecimal(subtotal);
        String valorDesconto = formatDecimal(desconto);
        String valorFrete = formatDecimal(frete);
        String valorTotalFinal = formatDecimal(total);
        String descricaoFormaPagamento = sale.getPaymentMethod() != null ? sale.getPaymentMethod().getDescription() : "—";
        String observacaoVenda = sale.getNotes();
        Integer quantidadeParcelas = sale.getInstallmentsCount();
        String detalheParcelas = null;
        if (quantidadeParcelas != null && quantidadeParcelas > 0 && total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal valorParcela = total.divide(BigDecimal.valueOf(quantidadeParcelas), 2, java.math.RoundingMode.HALF_UP);
            detalheParcelas = quantidadeParcelas + "x de R$ " + formatDecimal(valorParcela);
        }

        String enderecoEmpresa = buildEnderecoEmpresa(tenant);
        String enderecoCliente = buildEnderecoCliente(sale);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
        String dataVenda = sale.getSaleDate() != null ? sale.getSaleDate().format(fmt) : "—";
        String dataHora = sale.getSaleDate() != null ? sale.getSaleDate().format(fmtHora) : "—";
        String dataVencimento = sale.getSaleDate() != null ? sale.getSaleDate().plusDays(15).format(fmt) : "—";
        String logoUrl = tenant != null ? tenant.getLogoUrl() : null;

        Context context = new Context();
        context.setVariable("nomeLoja", nomeLoja);
        context.setVariable("informacoesExtras", null);
        context.setVariable("nomeCliente", nomeCliente);
        context.setVariable("nomeVendedor", nomeVendedor);
        context.setVariable("numeroVenda", numeroVenda);
        context.setVariable("listaItensVenda", listaItens);
        context.setVariable("valorTotalProdutos", valorTotalProdutos);
        context.setVariable("valorDesconto", valorDesconto);
        context.setVariable("valorFrete", valorFrete);
        context.setVariable("valorTotalFinal", valorTotalFinal);
        context.setVariable("descricaoFormaPagamento", descricaoFormaPagamento);
        context.setVariable("detalheParcelas", detalheParcelas);
        context.setVariable("observacaoVenda", observacaoVenda);
        context.setVariable("enderecoEmpresa", enderecoEmpresa);
        context.setVariable("enderecoCliente", enderecoCliente);
        context.setVariable("dataVenda", dataVenda);
        context.setVariable("dataHora", dataHora);
        context.setVariable("dataVencimento", dataVencimento);
        context.setVariable("logoUrl", logoUrl);

        String html = templateEngine.process("receipt/sale-simple-receipt", context);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useDefaultPageSize(80, 200, PdfRendererBuilder.PageSizeUnits.MM);
            org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(Jsoup.parse(html, "UTF-8"));
            builder.withW3cDocument(w3cDoc, "");
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar comprovante da venda: " + e.getMessage());
        }
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) return "0,00";
        return new DecimalFormat("#,##0.00", PT_BR).format(value);
    }

    private String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() <= max ? s : s.substring(0, max);
    }

    private String buildEnderecoEmpresa(Tenant tenant) {
        if (tenant == null) return "";
        var parts = new ArrayList<String>();
        if (tenant.getAddressStreet() != null && !tenant.getAddressStreet().isBlank()) {
            String rua = tenant.getAddressStreet();
            if (tenant.getAddressNumber() != null && !tenant.getAddressNumber().isBlank()) {
                rua += " " + tenant.getAddressNumber();
            }
            if (tenant.getAddressComplement() != null && !tenant.getAddressComplement().isBlank()) {
                rua += ", " + tenant.getAddressComplement();
            }
            parts.add(rua);
        }
        if (tenant.getAddressNeighborhood() != null && !tenant.getAddressNeighborhood().isBlank()) {
            parts.add(tenant.getAddressNeighborhood());
        }
        if (tenant.getAddressCity() != null && tenant.getAddressState() != null) {
            String cidade = tenant.getAddressCity() + "/" + tenant.getAddressState();
            if (tenant.getAddressZip() != null && !tenant.getAddressZip().isBlank()) {
                cidade = tenant.getAddressZip() + " " + cidade;
            }
            parts.add(cidade);
        }
        return String.join("\n", parts);
    }

    private String buildEnderecoCliente(SaleResponse sale) {
        if (sale.getDeliveryAddress() != null && !sale.getDeliveryAddress().isBlank()) {
            return sale.getDeliveryAddress().replace("; ", "\n").replace(";", "\n");
        }
        return "";
    }
}
