<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../common/header.jsp" %>

<!-- Hero Banner -->
<div class="hero-banner">
    <div class="container text-center">
        <h1 class="display-4 fw-bold">🌸 Chào mừng đến StarShop</h1>
        <p class="lead">Cửa hàng hoa tươi cao cấp - Giao hàng tận nơi</p>
        <a href="/products" class="btn btn-light btn-lg rounded-pill px-5 mt-3">
            Khám phá ngay
        </a>
    </div>
</div>

<!-- Sản phẩm nổi bật -->
<div class="container mb-5">
    <h2 class="section-title">Sản phẩm nổi bật</h2>
    <div class="row g-4">
        <c:choose>
            <c:when test="${not empty featuredProducts}">
                <c:forEach var="product" items="${featuredProducts}">
                    <div class="col-6 col-md-3">
                        <div class="card product-card h-100 shadow-sm">
                            <img src="${not empty product.imageUrl ? product.imageUrl : '/static/images/no-image.png'}"
                                 class="card-img-top" alt="${product.name}">
                            <div class="card-body">
                                <h6 class="card-title">${product.name}</h6>
                                <p class="price">${product.price} đ</p>
                                <a href="/products/${product.id}" class="btn btn-starshop btn-sm w-100">
                                    Xem chi tiết
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="col-12 text-center py-5">
                    <p class="text-muted">Chưa có sản phẩm nào.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>