<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN"/>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>
        Cửa hàng - StarShop
    </title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <style>

        .shop-banner {
            width: 100%;
            height: 280px;
            object-fit: cover;
            border-radius: 16px;
        }

        .banner-empty {
            height: 280px;
            border-radius: 16px;
            background:
                linear-gradient(
                    135deg,
                    #f8d7da,
                    #fff3cd
                );
        }

        .shop-logo {
            width: 120px;
            height: 120px;
            object-fit: cover;
            border-radius: 50%;
            border: 5px solid white;
            background: white;
        }

        .product-image {
            width: 100%;
            height: 220px;
            object-fit: cover;
        }

        .product-image-empty {
            height: 220px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f1f3f5;
            color: #6c757d;
        }

        .product-card {
            transition: 0.2s;
        }

        .product-card:hover {
            transform: translateY(-3px);
        }

        .old-price {
            text-decoration: line-through;
            color: #6c757d;
        }

    </style>

</head>

<body class="bg-light">

<div class="container py-4">

    <c:if test="${not empty errorMessage}">

        <div class="alert alert-warning mt-5">

            ${errorMessage}

        </div>

    </c:if>

    <c:if test="${not empty shopData}">

        <c:set var="shop"
               value="${shopData.shop}"/>

        <div class="position-relative mb-5">

            <c:choose>

                <c:when test="${not empty shop.banner}">

                    <img src="${shop.banner}"
                         class="shop-banner"
                         alt="${shop.name}">

                </c:when>

                <c:otherwise>

                    <div class="banner-empty">
                    </div>

                </c:otherwise>

            </c:choose>

            <div class="d-flex align-items-end gap-3 mt-3">

                <c:choose>

                    <c:when test="${not empty shop.logo}">

                        <img src="${shop.logo}"
                             class="shop-logo"
                             alt="${shop.name}">

                    </c:when>

                    <c:otherwise>

                        <div class="shop-logo d-flex align-items-center justify-content-center border">

                            <span class="fs-1">
                                🌸
                            </span>

                        </div>

                    </c:otherwise>

                </c:choose>

                <div class="flex-grow-1">

                    <h1 class="mb-1">

                        ${shop.name}

                    </h1>

                    <div class="text-muted mb-2">

                        ⭐

                        <fmt:formatNumber
                                value="${shopData.averageRating}"
                                maxFractionDigits="1"/>

                        / 5

                        ·

                        ${shopData.reviewCount}
                        đánh giá

                        ·

                        ${shopData.productCount}
                        sản phẩm

                    </div>

                </div>

            </div>

        </div>

        <div class="row g-4 mb-5">

            <div class="col-lg-8">

                <div class="card shadow-sm h-100">

                    <div class="card-body p-4">

                        <h4>
                            Giới thiệu cửa hàng
                        </h4>

                        <c:choose>

                            <c:when test="${not empty shop.description}">

                                <p class="mb-0">
                                    ${shop.description}
                                </p>

                            </c:when>

                            <c:otherwise>

                                <p class="text-muted mb-0">
                                    Cửa hàng chưa cập nhật mô tả.
                                </p>

                            </c:otherwise>

                        </c:choose>

                    </div>

                </div>

            </div>

            <div class="col-lg-4">

                <div class="card shadow-sm h-100">

                    <div class="card-body p-4">

                        <h4>
                            Thông tin liên hệ
                        </h4>

                        <c:if test="${not empty shop.address}">

                            <div class="mb-2">

                                <strong>
                                    Địa chỉ:
                                </strong>

                                ${shop.address}

                            </div>

                        </c:if>

                        <c:if test="${not empty shop.phone}">

                            <div class="mb-2">

                                <strong>
                                    Điện thoại:
                                </strong>

                                ${shop.phone}

                            </div>

                        </c:if>

                        <c:if test="${not empty shop.email}">

                            <div>

                                <strong>
                                    Email:
                                </strong>

                                ${shop.email}

                            </div>

                        </c:if>

                    </div>

                </div>

            </div>

        </div>

        <c:if test="${not empty shopData.promotions}">

            <div class="mb-5">

                <h3 class="mb-3">
                    Khuyến mãi đang áp dụng
                </h3>

                <div class="row g-3">

                    <c:forEach
                            items="${shopData.promotions}"
                            var="promotion">

                        <div class="col-md-6 col-lg-4">

                            <div class="card border-success shadow-sm h-100">

                                <div class="card-body">

                                    <div class="d-flex justify-content-between gap-2">

                                        <h5>
                                            ${promotion.name}
                                        </h5>

                                        <span class="badge text-bg-success">
                                            ${promotion.code}
                                        </span>

                                    </div>

                                    <c:if test="${not empty promotion.description}">

                                        <p class="text-muted">
                                            ${promotion.description}
                                        </p>

                                    </c:if>

                                    <div class="fw-bold fs-5">

                                        <c:choose>

                                            <c:when test="${promotion.type == 'PERCENT'}">

                                                Giảm

                                                <fmt:formatNumber
                                                        value="${promotion.discountValue}"
                                                        maxFractionDigits="0"/>

                                                %

                                            </c:when>

                                            <c:when test="${promotion.type == 'FIXED_AMOUNT'}">

                                                Giảm

                                                <fmt:formatNumber
                                                        value="${promotion.discountValue}"
                                                        groupingUsed="true"
                                                        maxFractionDigits="0"/>

                                                VNĐ

                                            </c:when>

                                            <c:when test="${promotion.type == 'FREE_SHIPPING'}">

                                                Miễn phí giao hàng

                                            </c:when>

                                            <c:otherwise>

                                                ${promotion.type}

                                            </c:otherwise>

                                        </c:choose>

                                    </div>

                                    <c:if test="${not empty promotion.minOrderAmount}">

                                        <div class="small text-muted mt-2">

                                            Đơn tối thiểu:

                                            <fmt:formatNumber
                                                    value="${promotion.minOrderAmount}"
                                                    groupingUsed="true"
                                                    maxFractionDigits="0"/>

                                            VNĐ

                                        </div>

                                    </c:if>

                                </div>

                            </div>

                        </div>

                    </c:forEach>

                </div>

            </div>

        </c:if>

        <c:if test="${not empty shopData.bestSellers}">

            <div class="mb-5">

                <h3 class="mb-3">
                    Sản phẩm bán chạy
                </h3>

                <div class="row g-3">

                    <c:forEach
                            items="${shopData.bestSellers}"
                            var="product">

                        <div class="col-md-6 col-lg-3">

                            <div class="card product-card shadow-sm h-100">

                                <c:choose>

                                    <c:when test="${not empty product.imageUrl}">

                                        <img src="${product.imageUrl}"
                                             class="product-image card-img-top"
                                             alt="${product.name}">

                                    </c:when>

                                    <c:otherwise>

                                        <div class="product-image-empty">
                                            Chưa có ảnh
                                        </div>

                                    </c:otherwise>

                                </c:choose>

                                <div class="card-body">

                                    <div class="small text-muted">
                                        ${product.categoryName}
                                    </div>

                                    <h5 class="mt-1">
                                        ${product.name}
                                    </h5>

                                    <div class="mb-2">

                                        ⭐

                                        <fmt:formatNumber
                                                value="${product.rating}"
                                                maxFractionDigits="1"/>

                                        · Đã bán
                                        ${product.soldCount}

                                    </div>

                                    <c:choose>

                                        <c:when test="${not empty product.discountPrice
                                                && product.discountPrice > 0
                                                && product.discountPrice < product.price}">

                                            <div class="fw-bold text-danger fs-5">

                                                <fmt:formatNumber
                                                        value="${product.discountPrice}"
                                                        groupingUsed="true"
                                                        maxFractionDigits="0"/>

                                                VNĐ

                                            </div>

                                            <div class="old-price">

                                                <fmt:formatNumber
                                                        value="${product.price}"
                                                        groupingUsed="true"
                                                        maxFractionDigits="0"/>

                                                VNĐ

                                            </div>

                                        </c:when>

                                        <c:otherwise>

                                            <div class="fw-bold fs-5">

                                                <fmt:formatNumber
                                                        value="${product.price}"
                                                        groupingUsed="true"
                                                        maxFractionDigits="0"/>

                                                VNĐ

                                            </div>

                                        </c:otherwise>

                                    </c:choose>

                                </div>

                            </div>

                        </div>

                    </c:forEach>

                </div>

            </div>

        </c:if>

        <div class="d-flex justify-content-between align-items-center mb-3">

            <h3 class="mb-0">
                Sản phẩm của cửa hàng
            </h3>

        </div>

        <form method="get"
              class="card card-body shadow-sm mb-4">

            <div class="row g-3">

                <div class="col-md-6">

                    <input type="text"
                           name="keyword"
                           value="${keyword}"
                           class="form-control"
                           placeholder="Tìm sản phẩm trong cửa hàng">

                </div>

                <div class="col-md-4">

                    <select name="categoryId"
                            class="form-select">

                        <option value="">
                            Tất cả danh mục
                        </option>

                        <c:forEach
                                items="${shopData.categories}"
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

                    <button type="submit"
                            class="btn btn-primary w-100">

                        Tìm kiếm

                    </button>

                </div>

            </div>

        </form>

        <div class="row g-4">

            <c:forEach
                    items="${shopData.products.content}"
                    var="product">

                <div class="col-md-6 col-lg-4">

                    <div class="card product-card shadow-sm h-100">

                        <c:choose>

                            <c:when test="${not empty product.imageUrl}">

                                <img src="${product.imageUrl}"
                                     class="product-image card-img-top"
                                     alt="${product.name}">

                            </c:when>

                            <c:otherwise>

                                <div class="product-image-empty">
                                    Chưa có ảnh
                                </div>

                            </c:otherwise>

                        </c:choose>

                        <div class="card-body d-flex flex-column">

                            <div class="text-muted small">

                                ${product.categoryName}

                            </div>

                            <h5 class="mt-1">

                                ${product.name}

                            </h5>

                            <p class="text-muted flex-grow-1">

                                ${product.description}

                            </p>

                            <div class="small mb-2">

                                ⭐

                                <fmt:formatNumber
                                        value="${product.rating}"
                                        maxFractionDigits="1"/>

                                · Đã bán
                                ${product.soldCount}

                            </div>

                            <c:choose>

                                <c:when test="${not empty product.discountPrice
                                        && product.discountPrice > 0
                                        && product.discountPrice < product.price}">

                                    <div>

                                        <span class="fw-bold text-danger fs-5">

                                            <fmt:formatNumber
                                                    value="${product.discountPrice}"
                                                    groupingUsed="true"
                                                    maxFractionDigits="0"/>

                                            VNĐ

                                        </span>

                                        <span class="old-price ms-2">

                                            <fmt:formatNumber
                                                    value="${product.price}"
                                                    groupingUsed="true"
                                                    maxFractionDigits="0"/>

                                            VNĐ

                                        </span>

                                    </div>

                                </c:when>

                                <c:otherwise>

                                    <div class="fw-bold fs-5">

                                        <fmt:formatNumber
                                                value="${product.price}"
                                                groupingUsed="true"
                                                maxFractionDigits="0"/>

                                        VNĐ

                                    </div>

                                </c:otherwise>

                            </c:choose>

                        </div>

                    </div>

                </div>

            </c:forEach>

            <c:if test="${empty shopData.products.content}">

                <div class="col-12">

                    <div class="alert alert-info text-center">

                        Không tìm thấy sản phẩm phù hợp.

                    </div>

                </div>

            </c:if>

        </div>

        <c:if test="${shopData.products.totalPages > 1}">

            <nav class="mt-4">

                <ul class="pagination justify-content-center">

                    <c:forEach
                            begin="0"
                            end="${shopData.products.totalPages - 1}"
                            var="i">

                        <c:url
                                var="pageUrl"
                                value="/shop/${shop.id}">

                            <c:param
                                    name="page"
                                    value="${i}"/>

                            <c:param
                                    name="keyword"
                                    value="${keyword}"/>

                            <c:if test="${not empty categoryId}">

                                <c:param
                                        name="categoryId"
                                        value="${categoryId}"/>

                            </c:if>

                        </c:url>

                        <li class="page-item ${i == shopData.products.number
                                ? 'active'
                                : ''}">

                            <a class="page-link"
                               href="${pageUrl}">

                                ${i + 1}

                            </a>

                        </li>

                    </c:forEach>

                </ul>

            </nav>

        </c:if>

    </c:if>

</div>

</body>

</html>