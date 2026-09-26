package vn.iotstar.starshop.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VendorStatisticData {

    private long totalProducts;

    private long activeProducts;

    private long outOfStockProducts;

    private long totalOrders;

    private long newOrders;

    private long processingOrders;

    private long deliveredOrders;

    private long totalSoldQuantity;

    private BigDecimal revenueToday;

    private BigDecimal revenueWeek;

    private BigDecimal revenueMonth;

    private BigDecimal revenueYear;

    private Map<String, Long> orderStatusCounts;

    private List<TopProduct> topProducts;

    private List<DailyRevenue> dailyRevenues;

    @Getter
    @AllArgsConstructor
    public static class TopProduct {

        private Long productId;

        private String productName;

        private long quantitySold;

        private BigDecimal revenue;
    }

    @Getter
    @AllArgsConstructor
    public static class DailyRevenue {

        private String label;

        private BigDecimal revenue;
    }

    @Getter
    @AllArgsConstructor
    public static class RangeStatistic {

        private String fromDate;

        private String toDate;

        private BigDecimal revenue;

        private long deliveredOrders;

        private long soldQuantity;
    }
}