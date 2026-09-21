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

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Cửa hàng của tôi</h2>

        <a href="${pageContext.request.contextPath}/vendor/shop/register"
           class="btn btn-primary">
            Đăng ký cửa hàng
        </a>
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

                    <div class="card-body">

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

                        <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}"
                           class="btn btn-outline-primary">
                            Xem chi tiết
                        </a>

                        <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/edit"
                           class="btn btn-outline-secondary">
                            Chỉnh sửa
                        </a>

                    </div>

                </div>

            </div>

        </c:forEach>

    </div>

</div>

</body>
</html>