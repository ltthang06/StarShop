<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Thống kê doanh thu - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">

        <div>

            <h2>
                Thống kê doanh thu
            </h2>

            <div class="text-muted">
                ${shop.name}
            </div>

        </div>

        <a class="btn btn-secondary"
           href="${pageContext.request.contextPath}/vendor/dashboard">
            Quay lại Dashboard
        </a>

    </div>

    <c:if test="${not empty errorMessage}">

        <div class="alert alert-danger">
            ${errorMessage}
        </div>

    </c:if>

    <div class="row g-3 mb-4">

        <div class="col-md-6 col-lg-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Hôm nay
                    </div>

                    <h3>

                        <fmt:formatNumber
                                value="${statistics.revenueToday}"
                                groupingUsed="true"
                                maxFractionDigits="0"/>

                        VNĐ

                    </h3>

                </div>

            </div>

        </div>

        <div class="col-md-6 col-lg-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Tuần này
                    </div>

                    <h3>

                        <fmt:formatNumber
                                value="${statistics.revenueWeek}"
                                groupingUsed="true"
                                maxFractionDigits="0"/>

                        VNĐ

                    </h3>

                </div>

            </div>

        </div>

        <div class="col-md-6 col-lg-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Tháng này
                    </div>

                    <h3>

                        <fmt:formatNumber
                                value="${statistics.revenueMonth}"
                                groupingUsed="true"
                                maxFractionDigits="0"/>

                        VNĐ

                    </h3>

                </div>

            </div>

        </div>

        <div class="col-md-6 col-lg-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Năm nay
                    </div>

                    <h3>

                        <fmt:formatNumber
                                value="${statistics.revenueYear}"
                                groupingUsed="true"
                                maxFractionDigits="0"/>

                        VNĐ

                    </h3>

                </div>

            </div>

        </div>

    </div>

    <div class="row g-3 mb-4">

        <div class="col-md-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Tổng đơn
                    </div>

                    <h3>
                        ${statistics.totalOrders}
                    </h3>

                </div>

            </div>

        </div>

        <div class="col-md-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Đã giao
                    </div>

                    <h3>
                        ${statistics.deliveredOrders}
                    </h3>

                </div>

            </div>

        </div>

        <div class="col-md-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Sản phẩm đã bán
                    </div>

                    <h3>
                        ${statistics.totalSoldQuantity}
                    </h3>

                </div>

            </div>

        </div>

        <div class="col-md-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Hết hàng
                    </div>

                    <h3>
                        ${statistics.outOfStockProducts}
                    </h3>

                </div>

            </div>

        </div>

    </div>

    <div class="card shadow-sm mb-4">

        <div class="card-body">

            <h5 class="mb-3">
                Thống kê theo khoảng thời gian
            </h5>

            <form method="get"
                  class="row g-3 align-items-end">

                <div class="col-md-4">

                    <label class="form-label">
                        Từ ngày
                    </label>

                    <input type="date"
                           name="from"
                           value="${fromDate}"
                           class="form-control"
                           required>

                </div>

                <div class="col-md-4">

                    <label class="form-label">
                        Đến ngày
                    </label>

                    <input type="date"
                           name="to"
                           value="${toDate}"
                           class="form-control"
                           required>

                </div>

                <div class="col-md-4">

                    <button type="submit"
                            class="btn btn-primary w-100">
                        Thống kê
                    </button>

                </div>

            </form>

            <hr>

            <div class="row g-3">

                <div class="col-md-4">

                    <div class="border rounded p-3">

                        <div class="text-muted">
                            Doanh thu
                        </div>

                        <h4>

                            <fmt:formatNumber
                                    value="${rangeStatistic.revenue}"
                                    groupingUsed="true"
                                    maxFractionDigits="0"/>

                            VNĐ

                        </h4>

                    </div>

                </div>

                <div class="col-md-4">

                    <div class="border rounded p-3">

                        <div class="text-muted">
                            Đơn giao thành công
                        </div>

                        <h4>
                            ${rangeStatistic.deliveredOrders}
                        </h4>

                    </div>

                </div>

                <div class="col-md-4">

                    <div class="border rounded p-3">

                        <div class="text-muted">
                            Sản phẩm đã bán
                        </div>

                        <h4>
                            ${rangeStatistic.soldQuantity}
                        </h4>

                    </div>

                </div>

            </div>

        </div>

    </div>

    <div class="row g-4 mb-4">

        <div class="col-lg-8">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <h5 class="mb-4">
                        Doanh thu 7 ngày gần nhất
                    </h5>

                    <canvas id="revenueChart"
                            height="110">
                    </canvas>

                </div>

            </div>

        </div>

        <div class="col-lg-4">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <h5 class="mb-4">
                        Đơn theo trạng thái
                    </h5>

                    <canvas id="orderStatusChart">
                    </canvas>

                </div>

            </div>

        </div>

    </div>

    <div class="card shadow-sm">

        <div class="card-body">

            <h5 class="mb-3">
                Top 5 sản phẩm bán chạy
            </h5>

            <div class="table-responsive">

                <table class="table table-hover align-middle">

                    <thead>

                    <tr>
                        <th>Sản phẩm</th>
                        <th>Đã bán</th>
                        <th>Doanh thu</th>
                    </tr>

                    </thead>

                    <tbody>

                    <c:forEach
                            items="${statistics.topProducts}"
                            var="item">

                        <tr>

                            <td>
                                ${item.productName}
                            </td>

                            <td>
                                ${item.quantitySold}
                            </td>

                            <td>

                                <fmt:formatNumber
                                        value="${item.revenue}"
                                        groupingUsed="true"
                                        maxFractionDigits="0"/>

                                VNĐ

                            </td>

                        </tr>

                    </c:forEach>

                    <c:if test="${empty statistics.topProducts}">

                        <tr>

                            <td colspan="3"
                                class="text-center text-muted">

                                Chưa có dữ liệu bán hàng.

                            </td>

                        </tr>

                    </c:if>

                    </tbody>

                </table>

            </div>

        </div>

    </div>

</div>

<script>

    const revenueLabels = [

        <c:forEach
                items="${statistics.dailyRevenues}"
                var="item"
                varStatus="status">

            '${item.label}'${status.last ? '' : ','}

        </c:forEach>

    ];

    const revenueValues = [

        <c:forEach
                items="${statistics.dailyRevenues}"
                var="item"
                varStatus="status">

            ${item.revenue}${status.last ? '' : ','}

        </c:forEach>

    ];

    new Chart(
        document.getElementById(
            'revenueChart'
        ),
        {
            type: 'line',

            data: {

                labels: revenueLabels,

                datasets: [
                    {
                        label: 'Doanh thu (VNĐ)',
                        data: revenueValues,
                        tension: 0.3
                    }
                ]
            },

            options: {

                responsive: true,

                scales: {

                    y: {
                        beginAtZero: true
                    }
                }
            }
        }
    );

    const orderLabels = [

        <c:forEach
                items="${statistics.orderStatusCounts}"
                var="entry">

            <c:if test="${entry.value > 0}">
                '${entry.key}',
            </c:if>

        </c:forEach>

    ];

    const orderValues = [

        <c:forEach
                items="${statistics.orderStatusCounts}"
                var="entry">

            <c:if test="${entry.value > 0}">
                ${entry.value},
            </c:if>

        </c:forEach>

    ];

    new Chart(
        document.getElementById(
            'orderStatusChart'
        ),
        {
            type: 'doughnut',

            data: {

                labels: orderLabels,

                datasets: [
                    {
                        data: orderValues
                    }
                ]
            },

            options: {

                responsive: true,

                plugins: {

                    legend: {

                        position: 'top'
                    }
                }
            }
        }
    );

</script>

</body>

</html>