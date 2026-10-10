<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ taglib prefix="fn"
           uri="jakarta.tags.functions" %>

<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Vendor Dashboard - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">

        <div>

            <h2>
                Vendor Dashboard
            </h2>

            <p class="text-muted mb-0">
                Tổng quan cửa hàng của bạn
            </p>

        </div>

        <a href="${pageContext.request.contextPath}/vendor/shops"
           class="btn btn-primary">
            Quản lý cửa hàng
        </a>

    </div>

    <div class="row g-3 mb-4">

        <div class="col-md-6 col-lg-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Tổng cửa hàng
                    </div>

                    <h2 class="mb-0">
                        ${totalShops}
                    </h2>

                </div>

            </div>

        </div>

        <div class="col-md-6 col-lg-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Đang chờ duyệt
                    </div>

                    <h2 class="mb-0">
                        ${pendingShops}
                    </h2>

                </div>

            </div>

        </div>

        <div class="col-md-6 col-lg-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Đang hoạt động
                    </div>

                    <h2 class="mb-0">
                        ${activeShops}
                    </h2>

                </div>

            </div>

        </div>

        <div class="col-md-6 col-lg-3">

            <div class="card shadow-sm h-100">

                <div class="card-body">

                    <div class="text-muted">
                        Bị khóa
                    </div>

                    <h2 class="mb-0">
                        ${blockedShops}
                    </h2>

                </div>

            </div>

        </div>

    </div>

    <c:if test="${not empty dashboardStats}">

        <div class="d-flex justify-content-between align-items-center mt-5 mb-3">

            <div>

                <h4 class="mb-1">
                    Tổng quan ${dashboardShop.name}
                </h4>

            </div>

            <a class="btn btn-outline-primary"
               href="${pageContext.request.contextPath}/vendor/shops/${dashboardShop.id}/statistics">
                Xem thống kê chi tiết
            </a>

        </div>

        <div class="row g-3 mb-4">

            <div class="col-md-4 col-lg-2">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <div class="text-muted">
                            Sản phẩm
                        </div>

                        <h3>
                            ${dashboardStats.totalProducts}
                        </h3>

                    </div>

                </div>

            </div>

            <div class="col-md-4 col-lg-2">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <div class="text-muted">
                            Đang bán
                        </div>

                        <h3>
                            ${dashboardStats.activeProducts}
                        </h3>

                    </div>

                </div>

            </div>

            <div class="col-md-4 col-lg-2">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <div class="text-muted">
                            Hết hàng
                        </div>

                        <h3>
                            ${dashboardStats.outOfStockProducts}
                        </h3>

                    </div>

                </div>

            </div>

            <div class="col-md-4 col-lg-2">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <div class="text-muted">
                            Đơn mới
                        </div>

                        <h3>
                            ${dashboardStats.newOrders}
                        </h3>

                    </div>

                </div>

            </div>

            <div class="col-md-4 col-lg-2">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <div class="text-muted">
                            Đang xử lý
                        </div>

                        <h3>
                            ${dashboardStats.processingOrders}
                        </h3>

                    </div>

                </div>

            </div>

            <div class="col-md-4 col-lg-2">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <div class="text-muted">
                            Đã giao
                        </div>

                        <h3>
                            ${dashboardStats.deliveredOrders}
                        </h3>

                    </div>

                </div>

            </div>

        </div>

        <div class="row g-3 mb-4">

            <div class="col-md-4">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <div class="text-muted">
                            Doanh thu hôm nay
                        </div>

                        <h3>

                            <fmt:formatNumber
                                    value="${dashboardStats.revenueToday}"
                                    groupingUsed="true"
                                    maxFractionDigits="0"/>

                            VNĐ

                        </h3>

                    </div>

                </div>

            </div>

            <div class="col-md-4">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <div class="text-muted">
                            Doanh thu tháng
                        </div>

                        <h3>

                            <fmt:formatNumber
                                    value="${dashboardStats.revenueMonth}"
                                    groupingUsed="true"
                                    maxFractionDigits="0"/>

                            VNĐ

                        </h3>

                    </div>

                </div>

            </div>

            <div class="col-md-4">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <div class="text-muted">
                            Doanh thu năm
                        </div>

                        <h3>

                            <fmt:formatNumber
                                    value="${dashboardStats.revenueYear}"
                                    groupingUsed="true"
                                    maxFractionDigits="0"/>

                            VNĐ

                        </h3>

                    </div>

                </div>

            </div>

        </div>

        <div class="row g-4 mb-5">

            <div class="col-lg-7">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <h5 class="mb-4">
                            Doanh thu 7 ngày gần nhất
                        </h5>

                        <canvas id="revenueChart"
                                height="120">
                        </canvas>

                    </div>

                </div>

            </div>

            <div class="col-lg-5">

                <div class="card shadow-sm h-100">

                    <div class="card-body">

                        <h5 class="mb-3">
                            Sản phẩm bán chạy
                        </h5>

                        <div class="table-responsive">

                            <table class="table align-middle">

                                <thead>

                                <tr>
                                    <th>Sản phẩm</th>
                                    <th>Đã bán</th>
                                </tr>

                                </thead>

                                <tbody>

                                <c:forEach
                                        items="${dashboardStats.topProducts}"
                                        var="item">

                                    <tr>

                                        <td>
                                            ${item.productName}
                                        </td>

                                        <td>
                                            ${item.quantitySold}
                                        </td>

                                    </tr>

                                </c:forEach>

                                <c:if test="${empty dashboardStats.topProducts}">

                                    <tr>

                                        <td colspan="2"
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

        </div>

    </c:if>

    <div class="card shadow-sm">

        <div class="card-body">

            <div class="d-flex justify-content-between align-items-center mb-3">

                <h4 class="mb-0">
                    Cửa hàng của tôi
                </h4>

                <a href="${pageContext.request.contextPath}/vendor/shop/register"
                   class="btn btn-outline-primary">

                    Đăng ký cửa hàng

                </a>

            </div>

            <c:if test="${empty shops}">

                <div class="alert alert-info">
                    Bạn chưa có cửa hàng nào.
                </div>

            </c:if>

            <c:if test="${not empty shops}">

                <div class="table-responsive">

                    <table class="table table-hover align-middle">

                        <thead>

                        <tr>
                            <th>ID</th>
                            <th>Tên cửa hàng</th>
                            <th>Trạng thái</th>
                            <th>Ngày tạo</th>
                            <th>Thao tác</th>
                        </tr>

                        </thead>

                        <tbody>

                        <c:forEach
                                items="${shops}"
                                var="shop">

                            <tr>

                                <td>
                                    ${shop.id}
                                </td>

                                <td>
                                    ${shop.name}
                                </td>

                                <td>

                                    <c:choose>

                                        <c:when test="${shop.status == 'ACTIVE'}">

                                            <span class="badge text-bg-success">
                                                ACTIVE
                                            </span>

                                        </c:when>

                                        <c:when test="${shop.status == 'PENDING'}">

                                            <span class="badge text-bg-warning">
                                                PENDING
                                            </span>

                                        </c:when>

                                        <c:otherwise>

                                            <span class="badge text-bg-danger">
                                                ${shop.status}
                                            </span>

                                        </c:otherwise>

                                    </c:choose>

                                </td>

                                <td>

                                    <c:if test="${not empty shop.createdAt}">

                                        ${fn:substring(shop.createdAt, 8, 10)}/${fn:substring(shop.createdAt, 5, 7)}/${fn:substring(shop.createdAt, 0, 4)}
                                        ${fn:substring(shop.createdAt, 11, 19)}

                                    </c:if>

                                </td>

                                <td>

                                    <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}"
                                       class="btn btn-sm btn-outline-primary">
                                        Chi tiết
                                    </a>

                                    <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/edit"
                                       class="btn btn-sm btn-outline-secondary">
                                        Chỉnh sửa
                                    </a>

                                    <c:if test="${shop.status == 'ACTIVE'}">

                                        <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/products"
                                           class="btn btn-sm btn-outline-primary">
                                            Sản phẩm
                                        </a>

                                        <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/orders"
                                           class="btn btn-sm btn-outline-primary">
                                            Đơn hàng
                                        </a>

                                        <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/promotions"
                                           class="btn btn-sm btn-outline-primary">
                                            Khuyến mãi
                                        </a>

                                        <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/statistics"
                                           class="btn btn-sm btn-outline-success">
                                            Thống kê
                                        </a>

                                    </c:if>

                                </td>

                            </tr>

                        </c:forEach>

                        </tbody>

                    </table>

                </div>

            </c:if>

        </div>

    </div>

</div>

<c:if test="${not empty dashboardStats}">

    <script>

        const revenueLabels = [

            <c:forEach
                    items="${dashboardStats.dailyRevenues}"
                    var="item"
                    varStatus="status">

                '${item.label}'${status.last ? '' : ','}

            </c:forEach>

        ];

        const revenueValues = [

            <c:forEach
                    items="${dashboardStats.dailyRevenues}"
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

    </script>

</c:if>

</body>

</html>