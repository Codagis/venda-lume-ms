package com.vendalume.vendalume.api.dto.delivery;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dados do mapa OSM para exibir localização e métricas de entrega")
public class DeliveryMapOsmResponse {

    @Schema(description = "Latitude da origem (empresa)")
    private Double originLat;

    @Schema(description = "Longitude da origem (empresa)")
    private Double originLon;

    @Schema(description = "Latitude do destino (entrega)")
    private Double destLat;

    @Schema(description = "Longitude do destino (entrega)")
    private Double destLon;

    @Schema(description = "Endereço da origem")
    private String originAddress;

    @Schema(description = "Endereço do destino")
    private String destAddress;

    @Schema(description = "Distância formatada (ex: 5,2 km)")
    private String distanceText;

    @Schema(description = "Distância em km")
    private String distanceKm;

    @Schema(description = "Tempo estimado a pé")
    private String byFoot;

    @Schema(description = "Tempo estimado de bicicleta")
    private String byBike;

    @Schema(description = "Tempo estimado de carro ou moto")
    private String byCar;
}
