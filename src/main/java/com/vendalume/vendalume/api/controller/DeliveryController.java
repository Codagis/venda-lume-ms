package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.delivery.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.sale.SaleResponse;
import com.vendalume.vendalume.service.DeliveryService;
import com.vendalume.vendalume.service.GoogleMapsService;
import com.vendalume.vendalume.service.OsmMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller de entregas e acompanhamento.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@DefaultApiResponses
@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
@Tag(name = "Entregas", description = "Gestão de entregas e acompanhamento")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final GoogleMapsService googleMapsService;
    private final OsmMapService osmMapService;

    @PostMapping("/search")
    @Operation(summary = "Buscar entregas")
    public ResponseEntity<PageResponse<DeliveryResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @RequestBody DeliveryFilterRequest filter) {
        return ResponseEntity.ok(deliveryService.search(tenantId, filter));
    }

    @GetMapping("/active")
    @Operation(summary = "Listar entregas ativas")
    public ResponseEntity<List<DeliveryResponse>> listActive(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(deliveryService.listActive(tenantId));
    }

    @GetMapping("/my")
    @Operation(summary = "Minhas entregas", description = "Lista entregas atribuídas ao entregador logado")
    public ResponseEntity<List<DeliveryResponse>> listMyDeliveries() {
        return ResponseEntity.ok(deliveryService.listMyDeliveries());
    }

    @GetMapping("/sales-without-delivery")
    @Operation(summary = "Vendas delivery sem entrega cadastrada")
    public ResponseEntity<List<SaleResponse>> listSalesWithoutDelivery(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(deliveryService.listSalesWithoutDelivery(tenantId));
    }

    @GetMapping("/delivery-persons")
    @Operation(summary = "Listar entregadores disponíveis")
    public ResponseEntity<List<DeliveryPersonOption>> listDeliveryPersons(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(deliveryService.listDeliveryPersons(tenantId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar entrega por ID")
    public ResponseEntity<DeliveryResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.findById(id));
    }

    @GetMapping("/{id}/map-embed-url")
    @Operation(summary = "URL do mapa da entrega", description = "Retorna embedUrl e searchUrl para exibir o endereço no Google Maps. Usa integração Google Cloud.")
    public ResponseEntity<Map<String, String>> getMapEmbedUrl(@PathVariable UUID id) {
        DeliveryResponse delivery = deliveryService.findById(id);
        String embedUrl = googleMapsService.buildEmbedUrl(delivery);
        String searchUrl = googleMapsService.buildSearchUrl(delivery);
        return ResponseEntity.ok(Map.of(
                "enabled", String.valueOf(googleMapsService.isEnabled()),
                "embedUrl", embedUrl != null ? embedUrl : "",
                "searchUrl", searchUrl != null ? searchUrl : ""
        ));
    }

    @GetMapping("/{id}/map-metrics")
    @Operation(summary = "Métricas do mapa da entrega", description = "Distância e tempo estimado por modo de transporte (a pé, bicicleta, carro/moto) entre a empresa e o endereço de entrega.")
    public ResponseEntity<DeliveryMapMetricsResponse> getMapMetrics(@PathVariable UUID id) {
        DeliveryResponse delivery = deliveryService.findById(id);
        String originAddress = deliveryService.getTenantOriginAddress(id);
        DeliveryMapMetricsResponse metrics = googleMapsService.fetchDistanceMetrics(originAddress, delivery);
        return ResponseEntity.ok(metrics != null ? metrics : new DeliveryMapMetricsResponse());
    }

    @GetMapping("/{id}/map-osm")
    @Operation(summary = "Dados do mapa OSM", description = "Coordenadas e métricas via Leaflet + OpenStreetMap (Nominatim + OSRM). 100% gratuito, sem API key.")
    public ResponseEntity<DeliveryMapOsmResponse> getMapOsm(@PathVariable UUID id) {
        DeliveryResponse delivery = deliveryService.findById(id);
        String originAddress = deliveryService.getTenantOriginAddress(id);
        DeliveryMapOsmResponse data = osmMapService.fetchMapData(originAddress, delivery);
        return ResponseEntity.ok(data != null ? data : new DeliveryMapOsmResponse());
    }

    @PostMapping
    @Operation(summary = "Criar entrega a partir de venda")
    public ResponseEntity<DeliveryResponse> create(@Valid @RequestBody DeliveryCreateRequest request) {
        DeliveryResponse response = deliveryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Atribuir entregador")
    public ResponseEntity<DeliveryResponse> assign(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryAssignRequest request) {
        return ResponseEntity.ok(deliveryService.assign(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status da entrega")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar entrega", description = "Atualiza endereço, destinatário, instruções, agendamento e prioridade.")
    public ResponseEntity<DeliveryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryUpdateRequest request) {
        return ResponseEntity.ok(deliveryService.update(id, request));
    }
}
