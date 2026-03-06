package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.table.*;
import com.vendalume.vendalume.service.ReservationReceiptPdfService;
import com.vendalume.vendalume.service.ReservationService;
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
 * Controller de reservas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_RESERVATIONS, description = "Reservas de mesas")
@DefaultApiResponses
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationReceiptPdfService reservationReceiptPdfService;

    @Operation(summary = "Criar reserva")
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationCreateRequest request) {
        ReservationResponse response = reservationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar reserva por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @Operation(summary = "Buscar reservas com filtros")
    @PostMapping("/search")
    public ResponseEntity<PageResponse<ReservationResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody ReservationFilterRequest filter) {
        return ResponseEntity.ok(reservationService.search(tenantId, filter));
    }

    @Operation(summary = "Atualizar reserva")
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReservationUpdateRequest request) {
        return ResponseEntity.ok(reservationService.update(id, request));
    }

    @Operation(summary = "Excluir reserva")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Baixar comprovante da reserva em PDF")
    @GetMapping(value = "/{id}/receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getReceiptPdf(@PathVariable UUID id) {
        byte[] pdf = reservationReceiptPdfService.generateReceiptPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprovante-reserva-" + id + ".pdf\"")
                .body(pdf);
    }
}
