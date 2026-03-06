package com.vendalume.vendalume.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vendalume.vendalume.api.dto.table.OrderItemResponse;
import com.vendalume.vendalume.api.dto.table.OrderResponse;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Serviço para geração do comprovante de comanda para cozinha em PDF.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class TableOrderKitchenPdfService {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));
    private static final DecimalFormatSymbols PT_BR = new DecimalFormatSymbols(new Locale("pt", "BR"));

    private final TableOrderService tableOrderService;
    private final TenantRepository tenantRepository;
    private final TemplateEngine templateEngine;

    public byte[] generateKitchenReceiptPdf(UUID orderId) {
        OrderResponse order = tableOrderService.getById(orderId);
        Tenant tenant = tenantRepository.findById(order.getTenantId()).orElse(null);

        String nomeEstabelecimento = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                ? tenant.getTradeName() : (tenant != null ? tenant.getName() : "VendaLume");
        String logoUrl = tenant != null ? tenant.getLogoUrl() : null;
        String mesa = order.getTableName() != null ? order.getTableName() : "—";
        String numeroComanda = order.getId() != null ? order.getId().toString().substring(0, 8).toUpperCase() : "—";
        String dataHora = order.getOpenedAt() != null
                ? DATE_TIME_FMT.format(order.getOpenedAt()) : "—";
        String observacoes = order.getNotes() != null && !order.getNotes().isBlank() ? order.getNotes() : "";
        boolean temObservacoes = !observacoes.isEmpty();

        List<KitchenItemDto> itens = new ArrayList<>();
        BigDecimal totalGeral = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (OrderItemResponse i : order.getItems()) {
                BigDecimal qty = i.getQuantity() != null ? i.getQuantity() : BigDecimal.ZERO;
                BigDecimal unit = i.getUnitPrice() != null ? i.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal total = qty.multiply(unit);
                totalGeral = totalGeral.add(total);
                itens.add(KitchenItemDto.builder()
                        .descricao(i.getProductName() != null ? i.getProductName() : "—")
                        .quantidade(formatQty(qty))
                        .valorUnitario(formatDecimal(unit))
                        .valorTotal(formatDecimal(total))
                        .build());
            }
        }
        String valorTotal = formatDecimal(totalGeral);

        Context context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("nomeEstabelecimento", nomeEstabelecimento);
        context.setVariable("mesa", mesa);
        context.setVariable("numeroComanda", numeroComanda);
        context.setVariable("dataHora", dataHora);
        context.setVariable("observacoes", observacoes);
        context.setVariable("temObservacoes", temObservacoes);
        context.setVariable("itens", itens);
        context.setVariable("valorTotal", valorTotal);

        String html = templateEngine.process("receipt/table-order-kitchen-receipt", context);

        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useDefaultPageSize(80, 297, PdfRendererBuilder.PageSizeUnits.MM);
            var w3cDoc = new W3CDom().fromJsoup(Jsoup.parse(html, "UTF-8"));
            builder.withW3cDocument(w3cDoc, "");
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar comanda para cozinha: " + e.getMessage());
        }
    }

    private String formatDecimal(BigDecimal v) {
        if (v == null) return "0,00";
        return new DecimalFormat("#,##0.00", PT_BR).format(v);
    }

    private String formatQty(BigDecimal v) {
        if (v == null) return "0";
        return new DecimalFormat("#,##0.####", PT_BR).format(v);
    }

    @lombok.Data
    @lombok.Builder
    public static class KitchenItemDto {
        private String descricao;
        private String quantidade;
        private String valorUnitario;
        private String valorTotal;
    }
}
