package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.analytics.SalesAnalyticsResponse;
import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import com.vendalume.vendalume.repository.SaleItemRepository;
import com.vendalume.vendalume.repository.SaleRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesAnalyticsService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Map<SaleType, String> SALE_TYPE_LABELS = Map.of(
            SaleType.PDV, "PDV (Presencial)",
            SaleType.DELIVERY, "Delivery",
            SaleType.TAKEAWAY, "Retirada",
            SaleType.ONLINE, "Online",
            SaleType.WHOLESALE, "Atacado",
            SaleType.CATERING, "Eventos"
    );

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;

    @Transactional(readOnly = true)
    public SalesAnalyticsResponse getAnalytics(UUID tenantIdParam, LocalDate startDate, LocalDate endDate) {
        UUID tenantId = resolveTenantId(tenantIdParam);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByTenantIdAndSaleDateBetween(tenantId, start, end).stream()
                .filter(s -> s.getStatus() != SaleStatus.CANCELLED)
                .toList();

        List<Object[]> productRows = saleItemRepository.findProductSalesByTenantAndPeriod(tenantId, start, end);

        String periodLabel = startDate.format(DATE_FORMAT) + " a " + endDate.format(DATE_FORMAT);

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SalesAnalyticsResponse.TopProductItem> topByQuantity = buildTopProducts(productRows, totalRevenue, true);
        List<SalesAnalyticsResponse.TopProductItem> topByRevenue = buildTopProducts(productRows, totalRevenue, false);

        List<SalesAnalyticsResponse.SalesBySegmentItem> salesBySegment = buildSalesBySegment(sales, totalRevenue);

        SalesAnalyticsResponse.SummaryItem summary = SalesAnalyticsResponse.SummaryItem.builder()
                .totalSales(sales.size())
                .totalRevenue(totalRevenue)
                .averageTicket(sales.isEmpty() ? BigDecimal.ZERO : totalRevenue.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP))
                .build();

        List<SalesAnalyticsResponse.RecommendationItem> recommendations = buildRecommendations(
                topByQuantity, topByRevenue, salesBySegment, summary);

        return SalesAnalyticsResponse.builder()
                .periodLabel(periodLabel)
                .topProductsByQuantity(topByQuantity)
                .topProductsByRevenue(topByRevenue)
                .salesBySegment(salesBySegment)
                .summary(summary)
                .recommendations(recommendations)
                .build();
    }

    private List<SalesAnalyticsResponse.TopProductItem> buildTopProducts(
            List<Object[]> rows, BigDecimal totalRevenue, boolean orderByQuantity) {
        BigDecimal totalQty = rows.stream()
                .map(r -> (BigDecimal) r[2])
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SalesAnalyticsResponse.TopProductItem> items = new ArrayList<>();
        for (Object[] r : rows) {
            UUID productId = r[0] != null ? (UUID) r[0] : null;
            String productName = r[1] != null ? (String) r[1] : "Produto sem nome";
            BigDecimal qty = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            BigDecimal revenue = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
            Long saleCount = r[4] != null ? ((Number) r[4]).longValue() : 0L;

            double pctQty = totalQty.compareTo(BigDecimal.ZERO) > 0
                    ? qty.divide(totalQty, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;
            double pctRev = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;

            items.add(SalesAnalyticsResponse.TopProductItem.builder()
                    .productId(productId != null ? productId.toString() : null)
                    .productName(productName)
                    .totalQuantity(qty)
                    .totalRevenue(revenue)
                    .saleCount(saleCount)
                    .percentOfTotal(orderByQuantity ? pctQty : pctRev)
                    .build());
        }

        if (orderByQuantity) {
            items.sort((a, b) -> b.getTotalQuantity().compareTo(a.getTotalQuantity()));
        } else {
            items.sort((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()));
        }

        return items.stream().limit(15).collect(Collectors.toList());
    }

    private List<SalesAnalyticsResponse.SalesBySegmentItem> buildSalesBySegment(
            List<Sale> sales, BigDecimal totalRevenue) {
        Map<SaleType, List<Sale>> byType = sales.stream().collect(Collectors.groupingBy(Sale::getSaleType));

        SaleType leader = null;
        BigDecimal leaderTotal = BigDecimal.ZERO;
        for (SaleType t : SaleType.values()) {
            List<Sale> list = byType.getOrDefault(t, List.of());
            BigDecimal segTotal = list.stream().map(Sale::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (segTotal.compareTo(leaderTotal) > 0) {
                leaderTotal = segTotal;
                leader = t;
            }
        }

        List<SalesAnalyticsResponse.SalesBySegmentItem> result = new ArrayList<>();
        for (SaleType type : SaleType.values()) {
            List<Sale> list = byType.getOrDefault(type, List.of());
            BigDecimal segTotal = list.stream().map(Sale::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            double pct = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? segTotal.divide(totalRevenue, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;

            result.add(SalesAnalyticsResponse.SalesBySegmentItem.builder()
                    .type(type.name())
                    .label(SALE_TYPE_LABELS.getOrDefault(type, type.name()))
                    .saleCount(list.size())
                    .totalRevenue(segTotal)
                    .percentOfTotal(pct)
                    .isLeader(type == leader)
                    .build());
        }

        result.sort((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()));
        return result.stream().filter(s -> s.getSaleCount() > 0 || s.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    private List<SalesAnalyticsResponse.RecommendationItem> buildRecommendations(
            List<SalesAnalyticsResponse.TopProductItem> topByQty,
            List<SalesAnalyticsResponse.TopProductItem> topByRev,
            List<SalesAnalyticsResponse.SalesBySegmentItem> segments,
            SalesAnalyticsResponse.SummaryItem summary) {

        List<SalesAnalyticsResponse.RecommendationItem> recs = new ArrayList<>();

        if (!topByQty.isEmpty()) {
            var best = topByQty.get(0);
            recs.add(SalesAnalyticsResponse.RecommendationItem.builder()
                    .type("PRODUCT_FOCUS")
                    .title("Produto mais vendido")
                    .description(String.format("%s é o produto mais vendido (%.1f%% das unidades). Mantenha estoque prioritário.", best.getProductName(), best.getPercentOfTotal()))
                    .action("Ver estoque e pedidos")
                    .build());
        }

        if (!topByRev.isEmpty() && topByRev.size() >= 2) {
            var second = topByRev.get(1);
            if (second.getPercentOfTotal() > 5) {
                recs.add(SalesAnalyticsResponse.RecommendationItem.builder()
                        .type("INVESTMENT")
                        .title("Onde investir mais")
                        .description(String.format("%s gera boa receita (%.1f%%). Considere campanhas ou promoções para ampliar vendas.", second.getProductName(), second.getPercentOfTotal()))
                        .action("Promover produto")
                        .build());
            }
        }

        var leaderSegment = segments.stream().filter(SalesAnalyticsResponse.SalesBySegmentItem::isLeader).findFirst();
        if (leaderSegment.isPresent()) {
            var seg = leaderSegment.get();
            recs.add(SalesAnalyticsResponse.RecommendationItem.builder()
                    .type("SEGMENT_LEADER")
                    .title("Canal que mais vende")
                    .description(String.format("%s é o segmento líder com %.1f%% do faturamento e %d vendas.", seg.getLabel(), seg.getPercentOfTotal(), seg.getSaleCount()))
                    .action("Fortalecer este canal")
                    .build());
        }

        var secondSegment = segments.size() >= 2 ? segments.get(1) : null;
        if (secondSegment != null && secondSegment.getPercentOfTotal() > 5) {
            recs.add(SalesAnalyticsResponse.RecommendationItem.builder()
                    .type("SEGMENT_GROWTH")
                    .title("Canal com potencial")
                    .description(String.format("%s representa %.1f%% das vendas. Há espaço para crescer com marketing direcionado.", secondSegment.getLabel(), secondSegment.getPercentOfTotal()))
                    .action("Investir em divulgação")
                    .build());
        }

        if (summary.getTotalSales() > 0 && summary.getAverageTicket() != null) {
            recs.add(SalesAnalyticsResponse.RecommendationItem.builder()
                    .type("TICKET")
                    .title("Ticket médio")
                    .description(String.format("Ticket médio de R$ %.2f. Sugestões de aumento: combos, upsell e itens complementares.", summary.getAverageTicket().doubleValue()))
                    .action("Criar combos")
                    .build());
        }

        return recs;
    }

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return requestTenantId != null
                    ? requestTenantId
                    : SecurityUtils.getTenantIdOptional()
                    .orElseThrow(() -> new IllegalStateException("Selecione uma empresa para visualizar a análise."));
        }
        return SecurityUtils.requireTenantId();
    }
}
