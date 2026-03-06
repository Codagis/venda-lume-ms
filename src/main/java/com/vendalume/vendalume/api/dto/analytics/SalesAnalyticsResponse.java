package com.vendalume.vendalume.api.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta da API de análise de vendas e estratégias.
 * Produtos mais vendidos, segmentos e recomendações.
 */
/**
 * DTO de resposta com análise de vendas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Análise de vendas e estratégias para o negócio")
public class SalesAnalyticsResponse {

    @Schema(description = "Período analisado")
    private String periodLabel;

    @Schema(description = "Produtos mais vendidos por quantidade")
    private List<TopProductItem> topProductsByQuantity;

    @Schema(description = "Produtos com maior faturamento")
    private List<TopProductItem> topProductsByRevenue;

    @Schema(description = "Vendas por segmento/canal (PDV, Delivery, etc.)")
    private List<SalesBySegmentItem> salesBySegment;

    @Schema(description = "Resumo geral do período")
    private SummaryItem summary;

    @Schema(description = "Recomendações de onde investir")
    private List<RecommendationItem> recommendations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductItem {
        private String productId;
        private String productName;
        private BigDecimal totalQuantity;
        private BigDecimal totalRevenue;
        private long saleCount;
        private double percentOfTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesBySegmentItem {
        private String type;
        private String label;
        private long saleCount;
        private BigDecimal totalRevenue;
        private double percentOfTotal;
        private boolean isLeader;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryItem {
        private long totalSales;
        private BigDecimal totalRevenue;
        private BigDecimal averageTicket;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private String type;
        private String title;
        private String description;
        private String action;
    }
}
