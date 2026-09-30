package vn.iotstar.starshop.service.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.VendorStatisticData;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.repository.VendorStatisticRepository;
import vn.iotstar.starshop.service.ShopService;
import vn.iotstar.starshop.service.VendorStatisticService;

@Service
@RequiredArgsConstructor
public class VendorStatisticServiceImpl
        implements VendorStatisticService {

    private static final ZoneId VIETNAM_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter CHART_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM");

    private final VendorStatisticRepository statisticRepository;

    private final ShopService shopService;

    @Override
    @Transactional(readOnly = true)
    public VendorStatisticData getStatistics(
            Long shopId,
            Long ownerId) {

        shopService.getShopByOwner(
                shopId,
                ownerId
        );

        LocalDate today =
                LocalDate.now(VIETNAM_ZONE);

        LocalDateTime todayStart =
                today.atStartOfDay();

        LocalDateTime tomorrowStart =
                today.plusDays(1)
                        .atStartOfDay();

        LocalDate weekStartDate =
                today.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY
                        )
                );

        LocalDateTime weekStart =
                weekStartDate.atStartOfDay();

        LocalDateTime nextWeekStart =
                weekStartDate
                        .plusWeeks(1)
                        .atStartOfDay();

        LocalDate monthStartDate =
                today.withDayOfMonth(1);

        LocalDateTime monthStart =
                monthStartDate.atStartOfDay();

        LocalDateTime nextMonthStart =
                monthStartDate
                        .plusMonths(1)
                        .atStartOfDay();

        LocalDate yearStartDate =
                today.withDayOfYear(1);

        LocalDateTime yearStart =
                yearStartDate.atStartOfDay();

        LocalDateTime nextYearStart =
                yearStartDate
                        .plusYears(1)
                        .atStartOfDay();

        Map<String, Long> statusCounts =
                loadStatusCounts(shopId);

        long newOrders =
                statusCounts.getOrDefault(
                        OrderStatus.NEW.name(),
                        0L
                );

        long processingOrders =
                getProcessingOrders(
                        statusCounts
                );

        long deliveredOrders =
                statusCounts.getOrDefault(
                        OrderStatus.DELIVERED.name(),
                        0L
                );

        List<VendorStatisticData.TopProduct>
                topProducts =
                loadTopProducts(shopId);

        List<VendorStatisticData.DailyRevenue>
                dailyRevenues =
                loadSevenDayRevenue(
                        shopId,
                        today
                );

        return VendorStatisticData.builder()
                .totalProducts(
                        statisticRepository
                                .countProducts(shopId)
                )
                .activeProducts(
                        statisticRepository
                                .countActiveProducts(shopId)
                )
                .outOfStockProducts(
                        statisticRepository
                                .countOutOfStockProducts(shopId)
                )
                .totalOrders(
                        statisticRepository
                                .countOrders(shopId)
                )
                .newOrders(newOrders)
                .processingOrders(processingOrders)
                .deliveredOrders(deliveredOrders)
                .totalSoldQuantity(
                        toLong(
                                statisticRepository
                                        .sumAllSoldQuantity(
                                                shopId
                                        )
                        )
                )
                .revenueToday(
                        getRevenue(
                                shopId,
                                todayStart,
                                tomorrowStart
                        )
                )
                .revenueWeek(
                        getRevenue(
                                shopId,
                                weekStart,
                                nextWeekStart
                        )
                )
                .revenueMonth(
                        getRevenue(
                                shopId,
                                monthStart,
                                nextMonthStart
                        )
                )
                .revenueYear(
                        getRevenue(
                                shopId,
                                yearStart,
                                nextYearStart
                        )
                )
                .orderStatusCounts(statusCounts)
                .topProducts(topProducts)
                .dailyRevenues(dailyRevenues)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VendorStatisticData.RangeStatistic
            getRangeStatistics(
                    Long shopId,
                    Long ownerId,
                    LocalDate fromDate,
                    LocalDate toDate) {

        shopService.getShopByOwner(
                shopId,
                ownerId
        );

        if (fromDate == null
                || toDate == null) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn đầy đủ khoảng thời gian."
            );
        }

        if (toDate.isBefore(fromDate)) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc phải bằng hoặc sau ngày bắt đầu."
            );
        }

        LocalDateTime from =
                fromDate.atStartOfDay();

        LocalDateTime toExclusive =
                toDate.plusDays(1)
                        .atStartOfDay();

        BigDecimal revenue =
                getRevenue(
                        shopId,
                        from,
                        toExclusive
                );

        long deliveredOrders =
                statisticRepository
                        .countDeliveredOrders(
                                shopId,
                                from,
                                toExclusive
                        );

        long soldQuantity =
                toLong(
                        statisticRepository
                                .sumSoldQuantity(
                                        shopId,
                                        from,
                                        toExclusive
                                )
                );

        return new VendorStatisticData.RangeStatistic(
                fromDate.toString(),
                toDate.toString(),
                revenue,
                deliveredOrders,
                soldQuantity
        );
    }

    private Map<String, Long> loadStatusCounts(
            Long shopId) {

        Map<String, Long> result =
                new LinkedHashMap<>();

        for (OrderStatus status
                : OrderStatus.values()) {

            result.put(
                    status.name(),
                    0L
            );
        }

        List<Object[]> rows =
                statisticRepository
                        .countOrdersByStatus(shopId);

        for (Object[] row : rows) {

            if (row[0] == null) {
                continue;
            }

            String status =
                    row[0].toString();

            long count =
                    toLong(row[1]);

            result.put(
                    status,
                    count
            );
        }

        return result;
    }

    private long getProcessingOrders(
            Map<String, Long> statusCounts) {

        return statusCounts.getOrDefault(
                        OrderStatus.CONFIRMED.name(),
                        0L
                )
                + statusCounts.getOrDefault(
                        OrderStatus.READY_FOR_PICKUP.name(),
                        0L
                )
                + statusCounts.getOrDefault(
                        OrderStatus.ASSIGNED.name(),
                        0L
                )
                + statusCounts.getOrDefault(
                        OrderStatus.PICKED_UP.name(),
                        0L
                )
                + statusCounts.getOrDefault(
                        OrderStatus.SHIPPING.name(),
                        0L
                );
    }

    private List<VendorStatisticData.TopProduct>
            loadTopProducts(
                    Long shopId) {

        List<VendorStatisticData.TopProduct>
                result =
                new ArrayList<>();

        List<Object[]> rows =
                statisticRepository
                        .findTopProducts(shopId);

        for (Object[] row : rows) {

            result.add(
                    new VendorStatisticData.TopProduct(
                            toLong(row[0]),
                            row[1] == null
                                    ? ""
                                    : row[1].toString(),
                            toLong(row[2]),
                            toBigDecimal(row[3])
                    )
            );
        }

        return result;
    }

    private List<VendorStatisticData.DailyRevenue>
            loadSevenDayRevenue(
                    Long shopId,
                    LocalDate today) {

        LocalDate firstDate =
                today.minusDays(6);

        LocalDateTime from =
                firstDate.atStartOfDay();

        LocalDateTime to =
                today.plusDays(1)
                        .atStartOfDay();

        List<Object[]> rows =
                statisticRepository
                        .findDailyRevenue(
                                shopId,
                                from,
                                to
                        );

        Map<LocalDate, BigDecimal>
                revenueByDate =
                new LinkedHashMap<>();

        for (Object[] row : rows) {

            if (row[0] == null) {
                continue;
            }

            LocalDate date =
                    LocalDate.parse(
                            row[0].toString()
                    );

            revenueByDate.put(
                    date,
                    toBigDecimal(row[1])
            );
        }

        List<VendorStatisticData.DailyRevenue>
                result =
                new ArrayList<>();

        for (int i = 0; i < 7; i++) {

            LocalDate date =
                    firstDate.plusDays(i);

            result.add(
                    new VendorStatisticData.DailyRevenue(
                            date.format(
                                    CHART_DATE_FORMAT
                            ),
                            revenueByDate
                                    .getOrDefault(
                                            date,
                                            BigDecimal.ZERO
                                    )
                    )
            );
        }

        return result;
    }

    private BigDecimal getRevenue(
            Long shopId,
            LocalDateTime from,
            LocalDateTime to) {

        return toBigDecimal(
                statisticRepository
                        .sumRevenue(
                                shopId,
                                from,
                                to
                        )
        );
    }

    private BigDecimal toBigDecimal(
            Object value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        return new BigDecimal(
                value.toString()
        );
    }

    private long toLong(
            Object value) {

        if (value == null) {
            return 0L;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(
                value.toString()
        );
    }
}