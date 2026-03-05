package com.vendalume.vendalume.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vendalume.vendalume.api.dto.table.ReservationResponse;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Serviço para geração do comprovante de reserva em PDF.
 */
@Service
@RequiredArgsConstructor
public class ReservationReceiptPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    private final ReservationService reservationService;
    private final TenantRepository tenantRepository;
    private final TemplateEngine templateEngine;

    public byte[] generateReceiptPdf(UUID reservationId) {
        ReservationResponse reservation = reservationService.getReservationForReceipt(reservationId);
        Tenant tenant = tenantRepository.findById(reservation.getTenantId()).orElse(null);

        String nomeEstabelecimento = tenant != null && tenant.getTradeName() != null && !tenant.getTradeName().isBlank()
                ? tenant.getTradeName() : (tenant != null ? tenant.getName() : "VendaLume");

        String nomeCliente = reservation.getCustomerName() != null ? reservation.getCustomerName() : "—";
        String telefone = reservation.getCustomerPhone() != null && !reservation.getCustomerPhone().isBlank()
                ? reservation.getCustomerPhone() : "—";
        String email = reservation.getCustomerEmail() != null && !reservation.getCustomerEmail().isBlank()
                ? reservation.getCustomerEmail() : "—";
        String mesa = reservation.getTableName() != null ? reservation.getTableName() : "—";
        String dataHora = reservation.getScheduledAt() != null
                ? DATE_TIME_FORMATTER.format(reservation.getScheduledAt()) : "—";
        Integer pessoas = reservation.getNumberOfGuests() != null ? reservation.getNumberOfGuests() : 0;
        String statusDesc = reservation.getStatus() != null ? reservation.getStatus().getDescription() : "—";
        String observacoes = reservation.getNotes() != null && !reservation.getNotes().isBlank()
                ? reservation.getNotes() : "";
        boolean temObservacoes = !observacoes.isEmpty();
        String numeroReserva = reservation.getId() != null ? reservation.getId().toString().substring(0, 8).toUpperCase() : "—";
        String logoUrl = tenant != null ? tenant.getLogoUrl() : null;

        Context context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("nomeEstabelecimento", nomeEstabelecimento);
        context.setVariable("nomeCliente", nomeCliente);
        context.setVariable("telefone", telefone);
        context.setVariable("email", email);
        context.setVariable("mesa", mesa);
        context.setVariable("dataHora", dataHora);
        context.setVariable("numeroPessoas", pessoas);
        context.setVariable("statusDesc", statusDesc);
        context.setVariable("observacoes", observacoes);
        context.setVariable("temObservacoes", temObservacoes);
        context.setVariable("numeroReserva", numeroReserva);

        String html = templateEngine.process("receipt/reservation-receipt", context);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useDefaultPageSize(80, 200, PdfRendererBuilder.PageSizeUnits.MM);
            var w3cDoc = new W3CDom().fromJsoup(Jsoup.parse(html, "UTF-8"));
            builder.withW3cDocument(w3cDoc, "");
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar comprovante da reserva: " + e.getMessage());
        }
    }
}
