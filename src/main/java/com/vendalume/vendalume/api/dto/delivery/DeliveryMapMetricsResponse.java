package com.vendalume.vendalume.api.dto.delivery;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Métricas do mapa da entrega: distância e tempo estimado por modo de transporte.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Métricas de distância e tempo da entrega")
public class DeliveryMapMetricsResponse {

    @Schema(description = "Distância em km")
    private String distanceKm;

    @Schema(description = "Distância formatada (ex: 5,2 km)")
    private String distanceText;

    @Schema(description = "Tempo estimado a pé")
    private String byFoot;

    @Schema(description = "Tempo estimado de bicicleta")
    private String byBike;

    @Schema(description = "Tempo estimado de carro ou moto")
    private String byCar;
}
