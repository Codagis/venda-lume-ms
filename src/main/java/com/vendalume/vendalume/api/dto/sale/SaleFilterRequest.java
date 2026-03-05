package com.vendalume.vendalume.api.dto.sale;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros para busca de vendas")
public class SaleFilterRequest {

    @JsonProperty("tenantId")
    @Schema(description = "ID da empresa. Apenas root pode filtrar por outra empresa.")
    private UUID tenantId;

    @Schema(description = "Status da venda")
    private SaleStatus status;

    @Schema(description = "Tipo da venda")
    private SaleType saleType;

    @Schema(description = "Data/hora inicial")
    private LocalDateTime startDate;

    @Schema(description = "Data/hora final")
    private LocalDateTime endDate;

    @Schema(description = "Busca por número, nome do cliente ou documento")
    private String search;

    @Schema(description = "Página (0-based)", example = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Quantidade por página", example = "20")
    @Builder.Default
    private Integer size = 20;
}
