package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller de comandas e pedidos de mesa.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_TABLE_ORDERS, description = "Comandas e pedidos de mesa")
@DefaultApiResponses
@RestController
@RequestMapping("/table-orders")
@RequiredArgsConstructor
public class TableOrderController {

    private final TableOrderService tableOrderService;
    private final TableOrderKitchenPdfService tableOrderKitchenPdfService;
    private final TableOrderAccountPdfService tableOrderAccountPdfService;

    @Operation(summary = "Abrir comanda")
    @PostMapping("/open")
    public ResponseEntity<OrderResponse> openOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = tableOrderService.openOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar comanda por ID")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tableOrderService.getById(id));
    }

    @Operation(summary = "Atualizar observações da comanda")
    @PatchMapping("/{id}/notes")
    public ResponseEntity<OrderResponse> updateNotes(
            @PathVariable UUID id,
            @RequestBody OrderNotesUpdateRequest request) {
        return ResponseEntity.ok(tableOrderService.updateNotes(id, request));
    }

    @Operation(summary = "Baixar comanda da cozinha em PDF")
    @GetMapping(value = "/{id}/kitchen-receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getKitchenReceiptPdf(@PathVariable UUID id) {
        byte[] pdf = tableOrderKitchenPdfService.generateKitchenReceiptPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comanda-cozinha-" + id + ".pdf\"")
                .body(pdf);
    }

    @Operation(summary = "Baixar conta em PDF")
    @GetMapping(value = "/{id}/account.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getAccountPdf(@PathVariable UUID id) {
        byte[] pdf = tableOrderAccountPdfService.generateAccountPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"conta-" + id + ".pdf\"")
                .body(pdf);
    }

    @Operation(summary = "Fechar comanda como pendente")
    @PostMapping("/{id}/close-pending")
    public ResponseEntity<OrderResponse> closeOrderAsPending(
            @PathVariable UUID id,
            @RequestBody(required = false) OrderClosePendingRequest request) {
        return ResponseEntity.ok(tableOrderService.closeOrderAsPending(id, request));
    }

    @Operation(summary = "Listar comandas abertas")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOpenOrders(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(tableOrderService.listOpenOrders(tenantId));
    }

    @Operation(summary = "Adicionar item à comanda")
    @PostMapping("/{id}/items")
    public ResponseEntity<OrderItemResponse> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody OrderItemAddRequest request) {
        OrderItemResponse response = tableOrderService.addItem(id, request.getProductId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Remover item da comanda")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID itemId) {
        tableOrderService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar quantidade do item")
    @PutMapping("/items/{itemId}/quantity")
    public ResponseEntity<OrderItemResponse> updateItemQuantity(
            @PathVariable UUID itemId,
            @Valid @RequestBody OrderItemQuantityUpdateRequest request) {
        return ResponseEntity.ok(tableOrderService.updateItemQuantity(itemId, request.getQuantity()));
    }

    @Operation(summary = "Fechar comanda")
    @PostMapping("/{id}/close")
    public ResponseEntity<OrderResponse> closeOrder(
            @PathVariable UUID id,
            @Valid @RequestBody OrderCloseRequest request) {
        return ResponseEntity.ok(tableOrderService.closeOrder(id, request));
    }

    @Operation(summary = "Cancelar comanda")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        tableOrderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
