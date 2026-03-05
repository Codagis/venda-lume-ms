package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.table.*;
import com.vendalume.vendalume.service.TableOrderAccountPdfService;
import com.vendalume.vendalume.service.TableOrderKitchenPdfService;
import com.vendalume.vendalume.service.TableOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/table-orders")
@RequiredArgsConstructor
public class TableOrderController {

    private final TableOrderService tableOrderService;
    private final TableOrderKitchenPdfService tableOrderKitchenPdfService;
    private final TableOrderAccountPdfService tableOrderAccountPdfService;

    @PostMapping("/open")
    public ResponseEntity<OrderResponse> openOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = tableOrderService.openOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tableOrderService.getById(id));
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<OrderResponse> updateNotes(
            @PathVariable UUID id,
            @RequestBody OrderNotesUpdateRequest request) {
        return ResponseEntity.ok(tableOrderService.updateNotes(id, request));
    }

    @GetMapping(value = "/{id}/kitchen-receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getKitchenReceiptPdf(@PathVariable UUID id) {
        byte[] pdf = tableOrderKitchenPdfService.generateKitchenReceiptPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comanda-cozinha-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping(value = "/{id}/account.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getAccountPdf(@PathVariable UUID id) {
        byte[] pdf = tableOrderAccountPdfService.generateAccountPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"conta-" + id + ".pdf\"")
                .body(pdf);
    }

    @PostMapping("/{id}/close-pending")
    public ResponseEntity<OrderResponse> closeOrderAsPending(
            @PathVariable UUID id,
            @RequestBody(required = false) OrderClosePendingRequest request) {
        return ResponseEntity.ok(tableOrderService.closeOrderAsPending(id, request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOpenOrders(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(tableOrderService.listOpenOrders(tenantId));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderItemResponse> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody OrderItemAddRequest request) {
        OrderItemResponse response = tableOrderService.addItem(id, request.getProductId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID itemId) {
        tableOrderService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{itemId}/quantity")
    public ResponseEntity<OrderItemResponse> updateItemQuantity(
            @PathVariable UUID itemId,
            @Valid @RequestBody OrderItemQuantityUpdateRequest request) {
        return ResponseEntity.ok(tableOrderService.updateItemQuantity(itemId, request.getQuantity()));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<OrderResponse> closeOrder(
            @PathVariable UUID id,
            @Valid @RequestBody OrderCloseRequest request) {
        return ResponseEntity.ok(tableOrderService.closeOrder(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        tableOrderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
