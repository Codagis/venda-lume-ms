package com.vendalume.vendalume.api.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationReceiptPdfService reservationReceiptPdfService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationCreateRequest request) {
        ReservationResponse response = reservationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @PostMapping("/search")
    public ResponseEntity<PageResponse<ReservationResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody ReservationFilterRequest filter) {
        return ResponseEntity.ok(reservationService.search(tenantId, filter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReservationUpdateRequest request) {
        return ResponseEntity.ok(reservationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getReceiptPdf(@PathVariable UUID id) {
        byte[] pdf = reservationReceiptPdfService.generateReceiptPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprovante-reserva-" + id + ".pdf\"")
                .body(pdf);
    }
}
