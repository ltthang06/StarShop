<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Quản lý sản phẩm - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <style>
        .product-thumbnail {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 8px;
            border: 1px solid #dee2e6;
        }

        .no-image {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            border: 1px solid #dee2e6;
            background: #f1f3f5;
            display: flex;
            align-items: center;
            justify-content: center;
            text-align: center;
            font-size: 12px;
            color: #6c757d;
        }

        .table td {
            vertical-align: middle;
        }

        .table th {
            vertical-align: middle;
        }
    </style>

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">

        <div>

            <h2>Quản lý sản phẩm</h2>

            <div class="text-muted">
                ${shop.name}
            </div>

        </div>

        <a class="btn btn-primary"
           href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/products/create">
            + Thêm sản phẩm
        </a>

    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            ${successMessage}
        </div>
    </c:if>

    <c:if test="${not empty warningMessage}">
        <div class="alert alert-warning">
            ${warningMessage}
        </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ${errorMessage}
        </div>
    </c:if>

    <form method="get"
          class="card card-body mb-4">

        <div class="row g-3">

            <div class="col-md-4">

                <input type="text"
                       name="keyword"
                       value="${keyword}"
                       class="form-control"
                       placeholder="Tên hoặc mô tả sản phẩm">

            </div>

            <div class="col-md-3">

                <select name="categoryId"
                        class="form-select">

                    <option value="">
                        Tất cả danh mục
                    </option>

                    <c:forEach items="${categories}"
                               var="category">

                        <option value="${category.id}"
                            ${categoryId == category.id
                                ? 'selected'
                                : ''}>
                            ${category.name}
                        </option>

                    </c:forEach>

                </select>

            </div>

            <div class="col-md-2">

                <select name="status"
                        class="form-select">

                    <option value="">
                        Tất cả trạng thái
                    </option>

                    <c:forEach items="${statuses}"
                               var="status">

                        <option value="${status}"
                            ${selectedStatus == status
                                ? 'selected'
                                : ''}>
                            ${status}
                        </option>

                    </c:forEach>

                </select>

            </div>

            <div class="col-md-1">

                <select name="size"
                        class="form-select">

                    <option value="5"
                        ${size == 5 ? 'selected' : ''}>
                        5
                    </option>

                    <option value="10"
                        ${size == 10 ? 'selected' : ''}>
                        10
                    </option>

                    <option value="20"
                        ${size == 20 ? 'selected' : ''}>
                        20
                    </option>

                </select>

            </div>

            <div class="col-md-2">

                <button type="submit"
                        class="btn btn-primary w-100">
                    Tìm kiếm
                </button>

            </div>

        </div>

    </form>

    <div class="card shadow-sm">

        <div class="table-responsive">

            <table class="table table-hover mb-0">

                <thead class="table-light">

                <tr>
                    <th>ID</th>
                    <th>Ảnh</th>
                    <th>Tên sản phẩm</th>
                    <th>Danh mục</th>
                    <th>Giá</th>
                    <th>Tồn kho</th>
                    <th>Đã bán</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>

                </thead>

                <tbody>

                <c:forEach items="${products.content}"
                           var="product">

                    <tr>

                        <td>
                            ${product.id}
                        </td>

                        <td>

                            <c:choose>

                                <c:when test="${not empty primaryImages[product.id]}">

                                    <img src="${primaryImages[product.id]}"
                                         class="product-thumbnail"
                                         alt="${product.name}">

                                </c:when>

                                <c:otherwise>

                                    <div class="no-image">
                                        Chưa có ảnh
                                    </div>

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <td>
                            ${product.name}
                        </td>

                        <td>
                            ${product.category.name}
                        </td>

                        <td>

                            <fmt:formatNumber
                                    value="${product.price}"
                                    type="number"
                                    groupingUsed="true"
                                    maxFractionDigits="0"/>
                            VNĐ

                        </td>

                        <td>
                            ${product.quantity}
                        </td>

                        <td>
                            ${product.soldCount}
                        </td>

                        <td>

                            <span class="badge text-bg-secondary">
                                ${product.status}
                            </span>

                        </td>

                        <td>

                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/products/${product.id}/edit">
                                Sửa
                            </a>

                            <form method="post"
                                  class="d-inline"
                                  action="${pageContext.request.contextPath}/vendor/shops/${shop.id}/products/${product.id}/delete"
                                  onsubmit="return confirm('Chuyển sản phẩm sang INACTIVE?')">

                                <input type="hidden"
                                       name="${_csrf.parameterName}"
                                       value="${_csrf.token}">

                                <button type="submit"
                                        class="btn btn-sm btn-outline-danger">
                                    Ngừng bán
                                </button>

                            </form>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty products.content}">

                    <tr>

                        <td colspan="9"
                            class="text-center py-4 text-muted">
                            Chưa có sản phẩm phù hợp.
                        </td>

                    </tr>

                </c:if>

                </tbody>

            </table>

        </div>

    </div>

    <c:if test="${products.totalPages > 0}">

        <div class="d-flex justify-content-center mt-4">

            <div class="btn-group">

                <c:forEach begin="0"
                           end="${products.totalPages - 1}"
                           var="i">

                    <c:url var="pageUrl"
                           value="/vendor/shops/${shop.id}/products">

                        <c:param name="page"
                                 value="${i}"/>

                        <c:param name="size"
                                 value="${size}"/>

                        <c:param name="keyword"
                                 value="${keyword}"/>

                        <c:if test="${not empty categoryId}">
                            <c:param name="categoryId"
                                     value="${categoryId}"/>
                        </c:if>

                        <c:if test="${not empty selectedStatus}">
                            <c:param name="status"
                                     value="${selectedStatus}"/>
                        </c:if>

                    </c:url>

                    <a href="${pageUrl}"
                       class="btn ${i == products.number
                           ? 'btn-primary'
                           : 'btn-outline-primary'}">
                        ${i + 1}
                    </a>

                </c:forEach>

            </div>

        </div>

    </c:if>

    <div class="mt-4">

        <a class="btn btn-secondary"
           href="${pageContext.request.contextPath}/vendor/dashboard">
            Quay lại Dashboard
        </a>

    </div>

</div>

</body>
</html>