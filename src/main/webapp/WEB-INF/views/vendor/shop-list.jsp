<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cửa hàng của tôi - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-4">
        <h2 class="mb-0">Cửa hàng của tôi</h2>

        <div class="d-flex gap-2 flex-wrap">
            <a href="${pageContext.request.contextPath}/vendor/dashboard"
               class="btn btn-outline-dark">
                Quay lại Dashboard
            </a>

            <a href="${pageContext.request.contextPath}/vendor/shop/register"
               class="btn btn-primary">
                Đăng ký cửa hàng
            </a>
        </div>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ${errorMessage}
        </div>
    </c:if>

    <c:if test="${empty shops}">
        <div class="alert alert-info">
            Bạn chưa có cửa hàng nào.
        </div>
    </c:if>

    <div class="row g-4">

        <c:forEach items="${shops}" var="shop">

            <div class="col-md-6 col-lg-4">

                <div class="card h-100 shadow-sm">

                    <div class="card-body d-flex flex-column">

                        <h4 class="card-title">
                            ${shop.name}
                        </h4>

                        <p class="mb-2">
                            Trạng thái:
                            <strong>${shop.status}</strong>
                        </p>

                        <p class="text-muted">
                            ${shop.description}
                        </p>

                        <div class="mt-auto">

                            <div class="d-flex gap-2 flex-wrap mb-2">

                                <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}"
                                   class="btn btn-outline-primary">
                                    Xem chi tiết
                                </a>

                                <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/edit"
                                   class="btn btn-outline-secondary">
                                    Chỉnh sửa
                                </a>

                            </div>

                            <div class="d-flex gap-2 flex-wrap">

                                <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/products"
                                   class="btn btn-sm btn-outline-success">
                                    Sản phẩm
                                </a>

                                <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/orders"
                                   class="btn btn-sm btn-outline-warning">
                                    Đơn hàng
                                </a>

                                <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/promotions"
                                   class="btn btn-sm btn-outline-info">
                                    Khuyến mãi
                                </a>

                                <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/statistics"
                                   class="btn btn-sm btn-outline-dark">
                                    Thống kê
                                </a>

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </c:forEach>

    </div>

</div>

</body>
</html>