package com.vendalume.vendalume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendalume.vendalume.api.dto.delivery.DeliveryMapOsmResponse;
import com.vendalume.vendalume.api.dto.delivery.DeliveryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Serviço para integração com OpenStreetMap (Nominatim + OSRM).
 * 100% gratuito, sem API key. Usa Leaflet no frontend para exibir o mapa.
 */
@Service
@Slf4j
public class OsmMapService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String OSRM_BASE = "https://router.project-osrm.org/route/v1";

    private final WebClient webClient = WebClient.builder()
            .defaultHeader("User-Agent", "VendaLume/1.0 (contact@vendalume.com)")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Geocodifica um endereço via Nominatim.
     *
     * @param address endereço completo
     * @return [lat, lon] ou null se não encontrado
     */
    public double[] geocode(String address) {
        if (address == null || address.isBlank()) return null;
        try {
            String body = webClient.get()
                    .uri(builder -> builder.scheme("https").host("nominatim.openstreetmap.org")
                            .path("/search")
                            .queryParam("q", address)
                            .queryParam("format", "json")
                            .queryParam("limit", "1")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if (body == null || body.isBlank()) return null;
            JsonNode root = objectMapper.readTree(body);
            if (root == null || !root.isArray() || root.isEmpty()) return null;
            JsonNode first = root.get(0);
            double lat = first.path("lat").asDouble(0);
            double lon = first.path("lon").asDouble(0);
            return new double[]{lat, lon};
        } catch (Exception e) {
            log.warn("Erro ao geocodificar endereço: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Busca rota e métricas via OSRM para um modo de transporte.
     *
     * @param lon1 longitude origem
     * @param lat1 latitude origem
     * @param lon2 longitude destino
     * @param lat2 latitude destino
     * @param profile driving, foot ou bicycle
     * @return [distanceMeters, durationSeconds] ou null
     */
    public double[] getRouteMetrics(double lon1, double lat1, double lon2, double lat2, String profile) {
        try {
            String coords = lon1 + "," + lat1 + ";" + lon2 + "," + lat2;
            String url = OSRM_BASE + "/" + profile + "/" + coords + "?overview=false";
            String body = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
            if (body == null || body.isBlank()) return null;
            JsonNode root = objectMapper.readTree(body);
            if (!"Ok".equals(root.path("code").asText(""))) return null;
            JsonNode routes = root.path("routes");
            if (routes.isEmpty()) return null;
            JsonNode route = routes.get(0);
            double dist = route.path("distance").asDouble(0);
            double dur = route.path("duration").asDouble(0);
            return new double[]{dist, dur};
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Formata duração em segundos para texto em português.
     */
    private String formatDuration(double seconds) {
        if (seconds <= 0) return null;
        int total = (int) Math.round(seconds);
        int hours = total / 3600;
        int mins = (total % 3600) / 60;
        if (hours > 0) {
            return String.format(Locale.ROOT, "%d h %d min", hours, mins);
        }
        if (mins > 0) {
            return String.format(Locale.ROOT, "%d min", mins);
        }
        return "menos de 1 min";
    }

    /**
     * Formata distância em metros para texto.
     */
    private String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format(Locale.ROOT, "%.0f m", meters);
        }
        return String.format(Locale.ROOT, "%.1f km", meters / 1000);
    }

    /**
     * Monta endereço simplificado para geocoding (Nominatim).
     * Formato: "rua número, bairro, cidade, estado, Brasil" - evita repetição.
     */
    private String buildFullAddress(DeliveryResponse d) {
        if (d == null) return null;
        List<String> parts = new ArrayList<>();
        String addr = d.getAddress() != null ? d.getAddress().trim() : "";
        if (!addr.isBlank()) {
            String street = addr.split("\\s*-\\s*")[0].trim().replaceAll("CEP:.*$", "").trim();
            if (!street.isBlank()) parts.add(street);
        }
        if (d.getNeighborhood() != null && !d.getNeighborhood().isBlank()) parts.add(d.getNeighborhood().trim());
        if (d.getCity() != null && !d.getCity().isBlank()) parts.add(d.getCity().trim());
        if (d.getState() != null && !d.getState().isBlank()) parts.add(d.getState().trim());
        parts.add("Brasil");
        return parts.size() <= 1 ? null : String.join(", ", parts);
    }

    /**
     * Busca dados do mapa OSM. Mostra coordenadas do destino (entrega).
     * Se houver endereço da empresa, inclui origem e métricas. Caso contrário, só o destino.
     */
    public DeliveryMapOsmResponse fetchMapData(String originAddress, DeliveryResponse delivery) {
        String destAddress = buildFullAddress(delivery);
        if (destAddress == null || destAddress.isBlank()) {
            return null;
        }
        try {
            double[] dest = geocode(destAddress);
            if (dest == null) {
                log.warn("Não foi possível geocodificar destino: {}", destAddress);
                return null;
            }
            double destLat = dest[0];
            double destLon = dest[1];

            Double originLat = null;
            Double originLon = null;
            if (originAddress != null && !originAddress.isBlank()) {
                Thread.sleep(1100); // Nominatim: 1 req/seg
                double[] origin = geocode(originAddress);
                if (origin != null) {
                    originLat = origin[0];
                    originLon = origin[1];
                }
            }

            String distanceText = null;
            String distanceKm = null;
            String byCar = null;
            String byBike = null;
            String byFoot = null;
            if (originLat != null && originLon != null) {
                double[] car = getRouteMetrics(originLon, originLat, destLon, destLat, "driving");
                double[] foot = getRouteMetrics(originLon, originLat, destLon, destLat, "foot");
                double[] bike = getRouteMetrics(originLon, originLat, destLon, destLat, "bicycle");

                if (car != null && car[0] > 0) {
                    distanceText = formatDistance(car[0]);
                    distanceKm = String.format(Locale.ROOT, "%.1f", car[0] / 1000);
                    byCar = formatDuration(car[1]);
                }
                if (foot != null && foot[1] > 0) byFoot = formatDuration(foot[1]);
                if (bike != null && bike[1] > 0) byBike = formatDuration(bike[1]);
            }

            return DeliveryMapOsmResponse.builder()
                    .originLat(originLat)
                    .originLon(originLon)
                    .destLat(destLat)
                    .destLon(destLon)
                    .originAddress(originAddress)
                    .destAddress(destAddress)
                    .distanceText(distanceText)
                    .distanceKm(distanceKm)
                    .byFoot(byFoot)
                    .byBike(byBike)
                    .byCar(byCar)
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Geocoding interrompido");
            return null;
        } catch (Exception e) {
            log.warn("Erro ao buscar dados OSM: {}", e.getMessage());
            return null;
        }
    }
}
