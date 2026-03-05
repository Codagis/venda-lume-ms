package com.vendalume.vendalume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vendalume.vendalume.api.dto.delivery.DeliveryMapMetricsResponse;
import com.vendalume.vendalume.api.dto.delivery.DeliveryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para integração com Google Maps (Maps Embed API e Distance Matrix API).
 * Utiliza a mesma infraestrutura Google Cloud do projeto.
 */
@Service
@Slf4j
public class GoogleMapsService {

    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    @Value("${vendalume.maps.api-key:}")
    private String apiKey;

    private final WebClient webClient = WebClient.create();

    /**
     * Gera a URL de embed do Google Maps para o endereço da entrega.
     *
     * @param delivery dados da entrega
     * @return URL do iframe embed ou null se não configurado
     */
    public String buildEmbedUrl(DeliveryResponse delivery) {
        if (apiKey == null || apiKey.isBlank()) return null;
        String address = buildFullAddress(delivery);
        if (address == null || address.isBlank()) return null;
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            return "https://www.google.com/maps/embed/v1/place?key=" + apiKey
                    + "&q=" + encoded
                    + "&zoom=16&language=pt-BR";
        } catch (Exception e) {
            log.warn("Erro ao montar URL do mapa: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gera a URL de busca no Google Maps (para abrir em nova aba).
     */
    public String buildSearchUrl(DeliveryResponse delivery) {
        String address = buildFullAddress(delivery);
        if (address == null || address.isBlank()) return null;
        try {
            return "https://www.google.com/maps/search/?api=1&query="
                    + URLEncoder.encode(address, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Busca métricas de distância e tempo estimado (a pé, bicicleta, carro/moto)
     * via Distance Matrix API. Origem = endereço da empresa, destino = endereço da entrega.
     */
    public DeliveryMapMetricsResponse fetchDistanceMetrics(String originAddress, DeliveryResponse delivery) {
        if (!isEnabled()) return null;
        String dest = buildFullAddress(delivery);
        if (originAddress == null || originAddress.isBlank() || dest == null || dest.isBlank()) return null;
        try {
            DistanceMatrixResult driving = fetchForMode(originAddress, dest, "driving");
            DistanceMatrixResult walking = fetchForMode(originAddress, dest, "walking");
            DistanceMatrixResult bicycling = fetchForMode(originAddress, dest, "bicycling");

            return DeliveryMapMetricsResponse.builder()
                    .distanceKm(driving != null && driving.km != null ? String.format("%.1f", driving.km) : null)
                    .distanceText(driving != null ? driving.distanceText : null)
                    .byFoot(walking != null ? walking.durationText : null)
                    .byBike(bicycling != null ? bicycling.durationText : null)
                    .byCar(driving != null ? driving.durationText : null)
                    .build();
        } catch (Exception e) {
            log.warn("Erro ao buscar métricas do Distance Matrix: {}", e.getMessage());
            return null;
        }
    }

    private DistanceMatrixResult fetchForMode(String origin, String dest, String mode) {
        try {
            JsonNode root = webClient.get()
                    .uri(builder -> builder.scheme("https").host("maps.googleapis.com")
                            .path("/maps/api/distancematrix/json")
                            .queryParam("origins", origin)
                            .queryParam("destinations", dest)
                            .queryParam("mode", mode)
                            .queryParam("key", apiKey)
                            .queryParam("language", "pt-BR")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null) return null;
            JsonNode elem = root.path("rows").path(0).path("elements").path(0);
            if (!"OK".equals(elem.path("status").asText(null))) return null;
            String distanceText = elem.path("distance").path("text").asText(null);
            int meters = elem.path("distance").path("value").asInt(0);
            String durationText = elem.path("duration").path("text").asText(null);
            return new DistanceMatrixResult(
                    distanceText,
                    meters > 0 ? meters / 1000.0 : null,
                    durationText
            );
        } catch (Exception e) {
            return null;
        }
    }

    private record DistanceMatrixResult(String distanceText, Double km, String durationText) {}

    private String buildFullAddress(DeliveryResponse d) {
        if (d == null) return null;
        List<String> parts = new ArrayList<>();
        if (d.getAddress() != null && !d.getAddress().isBlank()) parts.add(d.getAddress());
        if (d.getComplement() != null && !d.getComplement().isBlank()) parts.add(d.getComplement());
        if (d.getNeighborhood() != null && !d.getNeighborhood().isBlank()) parts.add(d.getNeighborhood());
        if (d.getCity() != null && !d.getCity().isBlank()) parts.add(d.getCity());
        if (d.getState() != null && !d.getState().isBlank()) parts.add(d.getState());
        if (d.getZipCode() != null && !d.getZipCode().isBlank()) parts.add(d.getZipCode());
        return parts.isEmpty() ? null : String.join(", ", parts);
    }
}
