package com.vendalume.vendalume.api.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de resposta com dados analíticos do dashboard.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados analíticos para o dashboard")
public class DashboardAnalyticsResponse {

    @Schema(description = "Vendas agregadas por dia (últimos 30 dias)")
    private List<SalesByDayItem> salesByDay;

    @Schema(description = "Vendas agregadas por tipo")
    private List<SalesByTypeItem> salesByType;

    @Schema(description = "Resumo do mês atual")
    private SummaryItem monthSummary;

    @Schema(description = "Resumo da semana atual")
    private SummaryItem weekSummary;

    @Schema(description = "Resumo de hoje")
    private SummaryItem todaySummary;

    @Schema(description = "Total de produtos cadastrados")
    private long productCount;

    @Schema(description = "Produtos com estoque baixo")
    private long lowStockCount;

    @Schema(description = "Resumo de contas a pagar")
    private CostControlSummaryItem accountsPayableSummary;

    @Schema(description = "Resumo de contas a receber")
    private CostControlSummaryItem accountsReceivableSummary;

    @Schema(description = "Contas a pagar por mês (últimos 6 meses)")
    private List<CostControlByMonthItem> payableByMonth;

    @Schema(description = "Contas a receber por mês (últimos 6 meses)")
    private List<CostControlByMonthItem> receivableByMonth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesByDayItem {
        private String date;
        private long count;
        private BigDecimal total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesByTypeItem {
        private String type;
        private String label;
        private long count;
        private BigDecimal total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryItem {
        private long count;
        private BigDecimal totalAmount;
        private BigDecimal subtotalAmount;
        private BigDecimal discountAmount;
        private BigDecimal deliveryFeeAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostControlSummaryItem {
        private BigDecimal totalPending;
        private BigDecimal totalOverdue;
        private long countPending;
        private long countOverdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostControlByMonthItem {
        private String month;
        private BigDecimal pending;
        private BigDecimal settled;
    }
}
