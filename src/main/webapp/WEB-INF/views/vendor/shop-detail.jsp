<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết cửa hàng - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container py-5">

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            ${successMessage}
        </div>
    </c:if>

    <div class="card shadow-sm">

        <div class="card-body p-4">

            <h2 class="mb-4">
                ${shop.name}
            </h2>

            <table class="table">

                <tr>
                    <th width="220">Mã cửa hàng</th>
                    <td>${shop.id}</td>
                </tr>

                <tr>
                    <th>Trạng thái</th>
                    <td>${shop.status}</td>
                </tr>

                <tr>
                    <th>Mô tả</th>
                    <td>${shop.description}</td>
                </tr>

                <tr>
                    <th>Số điện thoại</th>
                    <td>${shop.phone}</td>
                </tr>

                <tr>
                    <th>Email</th>
                    <td>${shop.email}</td>
                </tr>

                <tr>
                    <th>Địa chỉ</th>
                    <td>${shop.address}</td>
                </tr>

                <tr>
                    <th>Logo</th>
                    <td>${shop.logo}</td>
                </tr>

                <tr>
                    <th>Banner</th>
                    <td>${shop.banner}</td>
                </tr>

                <tr>
                    <th>Ngày tạo</th>
                    <td>${shop.createdAt}</td>
                </tr>

            </table>

            <a href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/edit"
               class="btn btn-primary">
                Chỉnh sửa
            </a>

            <a href="${pageContext.request.contextPath}/vendor/shops"
               class="btn btn-secondary">
                Quay lại
            </a>

        </div>

    </div>

</div>

</body>
</html>