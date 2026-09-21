<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Vendor Dashboard - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2>Vendor Dashboard</h2>
            <p class="text-muted mb-0">Tổng quan cửa hàng của bạn</p>
        </div>

        <a href="${pageContext.request.contextPath}/vendor/shops"
           class="btn btn-primary">
            Quản lý cửa hàng
        </a>
    </div>

    <div class="row g-4 mb-5">

        <div class="col-md-6 col-lg-3">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h6 class="text-muted">Tổng cửa hàng</h6>
                    <h2>${totalShops}</h2>
                </div>
            </div>
        </div>

        <div class="col-md-6 col-lg-3">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h6 class="text-muted">Đang chờ duyệt</h6>
                    <h2>${pendingShops}</h2>
                </div>
            </div>
        </div>

        <div class="col-md-6 col-lg-3">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h6 class="text-muted">Đang hoạt động</h6>
                    <h2>${activeShops}</h2>
                </div>
            </div>
        </div>

        <div class="col-md-6 col-lg-3">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h6 class="text-muted">Bị khóa</h6>
                    <h2>${blockedShops}</h2>
                </div>
            </div>
        </div>

    </div>

    <div class="card shadow-sm">
        <div class="card-body">

            <div class="d-flex justify-content-between align-items-center mb-3">
                <h4 class="mb-0">Cửa hàng của tôi</h4>

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

                        <c:forEach items="${shops}" var="shop">

                            <tr>

                                <td>${shop.id}</td>

                                <td>${shop.name}</td>

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

                                <td>${shop.createdAt}</td>

                                <td>
                                    <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}"
                                       class="btn btn-sm btn-outline-primary">
                                        Chi tiết
                                    </a>

                                    <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/edit"
                                       class="btn btn-sm btn-outline-secondary">
                                        Chỉnh sửa
                                    </a>
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

</body>
</html>