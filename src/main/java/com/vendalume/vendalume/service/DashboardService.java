package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.dashboard.DashboardAnalyticsResponse;
import com.vendalume.vendalume.domain.entity.AccountPayable;
import com.vendalume.vendalume.domain.entity.AccountReceivable;
import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.enums.AccountStatus;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import com.vendalume.vendalume.repository.AccountPayableRepository;
import com.vendalume.vendalume.repository.AccountReceivableRepository;
import com.vendalume.vendalume.repository.ProductRepository;
import com.vendalume.vendalume.repository.SaleRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM/yy");
    private static final Map<SaleType, String> SALE_TYPE_LABELS = Map.of(
            SaleType.PDV, "PDV",
            SaleType.DELIVERY, "Delivery",
            SaleType.TAKEAWAY, "Retirada",
            SaleType.ONLINE, "Online",
            SaleType.WHOLESALE, "Atacado",
            SaleType.CATERING, "Eventos"
    );

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final AccountPayableRepository accountPayableRepository;
    private final AccountReceivableRepository accountReceivableRepository;

    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getAnalytics(UUID tenantIdParam, LocalDate startDateParam, LocalDate endDateParam) {
        UUID tenantId = resolveTenantId(tenantIdParam);
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        LocalDate periodStart = startDateParam != null ? startDateParam : today.minusDays(30);
        LocalDate periodEnd = endDateParam != null ? endDateParam : today;
        if (periodStart.isAfter(periodEnd)) {
            periodStart = periodEnd;
        }
        LocalDateTime periodStartDt = periodStart.atStartOfDay();
        LocalDateTime periodEndDt = periodEnd.atTime(LocalTime.MAX);

        LocalDateTime monthStart = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime weekStart = now.toLocalDate().minusDays(now.getDayOfWeek().getValue() - 1).atStartOfDay();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();

        List<Sale> periodSales = findCompletedSales(tenantId, periodStartDt, periodEndDt);
        List<Sale> monthSales = findCompletedSales(tenantId, monthStart, now);
        List<Sale> weekSales = findCompletedSales(tenantId, weekStart, now);
        List<Sale> todaySales = findCompletedSales(tenantId, todayStart, now);

        List<DashboardAnalyticsResponse.SalesByDayItem> salesByDay = buildSalesByDay(periodSales, periodStart, periodEnd);
        List<DashboardAnalyticsResponse.SalesByTypeItem> salesByType = buildSalesByType(periodSales);

        long productCount = productRepository.countByTenantId(tenantId);
        long lowStockCount = productRepository.findLowStockProducts(tenantId).size();

        List<AccountPayable> payables = accountPayableRepository.findByTenantIdOrderByDueDateAsc(tenantId);
        List<AccountReceivable> receivables = accountReceivableRepository.findByTenantIdOrderByDueDateAsc(tenantId);
        DashboardAnalyticsResponse.CostControlSummaryItem payableSummary = buildPayableSummary(payables);
        DashboardAnalyticsResponse.CostControlSummaryItem receivableSummary = buildReceivableSummary(receivables);
        List<DashboardAnalyticsResponse.CostControlByMonthItem> payableByMonth = buildPayableByMonth(payables);
        List<DashboardAnalyticsResponse.CostControlByMonthItem> receivableByMonth = buildReceivableByMonth(receivables);

        DashboardAnalyticsResponse.SummaryItem periodSummary = toSummary(periodSales);
        return DashboardAnalyticsResponse.builder()
                .salesByDay(salesByDay)
                .salesByType(salesByType)
                .monthSummary(periodSummary)
                .weekSummary(toSummary(weekSales))
                .todaySummary(toSummary(todaySales))
                .productCount(productCount)
                .lowStockCount(lowStockCount)
                .accountsPayableSummary(payableSummary)
                .accountsReceivableSummary(receivableSummary)
                .payableByMonth(payableByMonth)
                .receivableByMonth(receivableByMonth)
                .build();
    }

    private DashboardAnalyticsResponse.CostControlSummaryItem buildPayableSummary(List<AccountPayable> list) {
        LocalDate today = LocalDate.now();
        BigDecimal totalPending = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        long countPending = 0;
        long countOverdue = 0;
        for (AccountPayable ap : list) {
            if (ap.getStatus() == AccountStatus.PAID || ap.getStatus() == AccountStatus.CANCELLED) continue;
            BigDecimal owed = (ap.getAmount() != null ? ap.getAmount() : BigDecimal.ZERO)
                    .subtract(ap.getPaidAmount() != null ? ap.getPaidAmount() : BigDecimal.ZERO);
            if (owed.compareTo(BigDecimal.ZERO) <= 0) continue;
            totalPending = totalPending.add(owed);
            countPending++;
            if (ap.getDueDate() != null && ap.getDueDate().isBefore(today)) {
                totalOverdue = totalOverdue.add(owed);
                countOverdue++;
            }
        }
        return DashboardAnalyticsResponse.CostControlSummaryItem.builder()
                .totalPending(totalPending)
                .totalOverdue(totalOverdue)
                .countPending(countPending)
                .countOverdue(countOverdue)
                .build();
    }

    private DashboardAnalyticsResponse.CostControlSummaryItem buildReceivableSummary(List<AccountReceivable> list) {
        LocalDate today = LocalDate.now();
        BigDecimal totalPending = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        long countPending = 0;
        long countOverdue = 0;
        for (AccountReceivable ar : list) {
            if (ar.getStatus() == AccountStatus.PAID || ar.getStatus() == AccountStatus.CANCELLED) continue;
            BigDecimal owed = (ar.getAmount() != null ? ar.getAmount() : BigDecimal.ZERO)
                    .subtract(ar.getReceivedAmount() != null ? ar.getReceivedAmount() : BigDecimal.ZERO);
            if (owed.compareTo(BigDecimal.ZERO) <= 0) continue;
            totalPending = totalPending.add(owed);
            countPending++;
            if (ar.getDueDate() != null && ar.getDueDate().isBefore(today)) {
                totalOverdue = totalOverdue.add(owed);
                countOverdue++;
            }
        }
        return DashboardAnalyticsResponse.CostControlSummaryItem.builder()
                .totalPending(totalPending)
                .totalOverdue(totalOverdue)
                .countPending(countPending)
                .countOverdue(countOverdue)
                .build();
    }

    private List<DashboardAnalyticsResponse.CostControlByMonthItem> buildPayableByMonth(List<AccountPayable> list) {
        return buildCostControlByMonth(
                list,
                ap -> ap.getDueDate(),
                ap -> (ap.getAmount() != null ? ap.getAmount() : BigDecimal.ZERO).subtract(ap.getPaidAmount() != null ? ap.getPaidAmount() : BigDecimal.ZERO),
                ap -> ap.getPaymentDate(),
                ap -> ap.getPaidAmount() != null ? ap.getPaidAmount() : BigDecimal.ZERO
        );
    }

    private List<DashboardAnalyticsResponse.CostControlByMonthItem> buildReceivableByMonth(List<AccountReceivable> list) {
        return buildCostControlByMonth(
                list,
                ar -> ar.getDueDate(),
                ar -> (ar.getAmount() != null ? ar.getAmount() : BigDecimal.ZERO).subtract(ar.getReceivedAmount() != null ? ar.getReceivedAmount() : BigDecimal.ZERO),
                ar -> ar.getReceiptDate(),
                ar -> ar.getReceivedAmount() != null ? ar.getReceivedAmount() : BigDecimal.ZERO
        );
    }

    private <T> List<DashboardAnalyticsResponse.CostControlByMonthItem> buildCostControlByMonth(
            List<T> list,
            java.util.function.Function<T, LocalDate> dueDateFn,
            java.util.function.Function<T, BigDecimal> pendingFn,
            java.util.function.Function<T, LocalDate> settledDateFn,
            java.util.function.Function<T, BigDecimal> settledAmountFn) {
        YearMonth now = YearMonth.now();
        Map<YearMonth, BigDecimal> pendingByMonth = new HashMap<>();
        Map<YearMonth, BigDecimal> settledByMonth = new HashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            pendingByMonth.put(ym, BigDecimal.ZERO);
            settledByMonth.put(ym, BigDecimal.ZERO);
        }
        for (T item : list) {
            LocalDate due = dueDateFn.apply(item);
            LocalDate settledDate = settledDateFn.apply(item);
            BigDecimal pending = pendingFn.apply(item);
            BigDecimal settled = settledAmountFn.apply(item);
            if (due != null && pending != null && pending.compareTo(BigDecimal.ZERO) > 0) {
                YearMonth ym = YearMonth.from(due);
                if (pendingByMonth.containsKey(ym)) {
                    pendingByMonth.merge(ym, pending, BigDecimal::add);
                }
            }
            if (settledDate != null && settled != null && settled.compareTo(BigDecimal.ZERO) > 0) {
                YearMonth ym = YearMonth.from(settledDate);
                if (settledByMonth.containsKey(ym)) {
                    settledByMonth.merge(ym, settled, BigDecimal::add);
                }
            }
        }
        List<DashboardAnalyticsResponse.CostControlByMonthItem> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            result.add(DashboardAnalyticsResponse.CostControlByMonthItem.builder()
                    .month(ym.format(MONTH_FORMAT))
                    .pending(pendingByMonth.getOrDefault(ym, BigDecimal.ZERO))
                    .settled(settledByMonth.getOrDefault(ym, BigDecimal.ZERO))
                    .build());
        }
        return result;
    }

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return requestTenantId != null
                    ? requestTenantId
                    : SecurityUtils.getTenantIdOptional()
                            .orElseThrow(() -> new IllegalStateException("Selecione uma empresa para visualizar o dashboard."));
        }
        return SecurityUtils.requireTenantId();
    }

    private List<Sale> findCompletedSales(UUID tenantId, LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByTenantIdAndSaleDateBetween(tenantId, start, end).stream()
                .filter(s -> s.getStatus() != SaleStatus.CANCELLED)
                .toList();
    }

    private List<DashboardAnalyticsResponse.SalesByDayItem> buildSalesByDay(List<Sale> sales, LocalDate from, LocalDate to) {
        Map<LocalDate, List<Sale>> byDate = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getSaleDate().toLocalDate()));

        List<DashboardAnalyticsResponse.SalesByDayItem> result = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            List<Sale> daySales = byDate.getOrDefault(d, List.of());
            BigDecimal total = daySales.stream().map(Sale::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(DashboardAnalyticsResponse.SalesByDayItem.builder()
                    .date(d.format(DATE_FORMAT))
                    .count(daySales.size())
                    .total(total)
                    .build());
        }
        return result;
    }

    private List<DashboardAnalyticsResponse.SalesByTypeItem> buildSalesByType(List<Sale> sales) {
        Map<SaleType, List<Sale>> byType = sales.stream().collect(Collectors.groupingBy(Sale::getSaleType));
        return Arrays.stream(SaleType.values())
                .map(type -> {
                    List<Sale> list = byType.getOrDefault(type, List.of());
                    BigDecimal total = list.stream().map(Sale::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return DashboardAnalyticsResponse.SalesByTypeItem.builder()
                            .type(type.name())
                            .label(SALE_TYPE_LABELS.getOrDefault(type, type.name()))
                            .count(list.size())
                            .total(total)
                            .build();
                })
                .toList();
    }

    private DashboardAnalyticsResponse.SummaryItem toSummary(List<Sale> sales) {
        long count = sales.size();
        BigDecimal totalAmount = sales.stream().map(Sale::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotalAmount = sales.stream().map(Sale::getSubtotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = sales.stream().map(Sale::getDiscountAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryFeeAmount = sales.stream().map(Sale::getDeliveryFee).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return DashboardAnalyticsResponse.SummaryItem.builder()
                .count(count)
                .totalAmount(totalAmount)
                .subtotalAmount(subtotalAmount)
                .discountAmount(discountAmount)
                .deliveryFeeAmount(deliveryFeeAmount)
                .build();
    }
}
