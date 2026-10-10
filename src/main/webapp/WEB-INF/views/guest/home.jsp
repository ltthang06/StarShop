<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jsp" %>

<fmt:setLocale value="vi_VN"/>
<c:url var="productsUrl" value="/products"/>
<c:url var="fallbackUrl" value="/images/no-image.svg"/>

<div class="hero-banner">
    <div class="container text-center">
        <h1 class="display-4 fw-bold">🌸 Chào mừng đến StarShop</h1>
        <p class="lead">Cửa hàng hoa tươi cao cấp - Giao hàng tận nơi</p>
        <a href="${productsUrl}" class="btn btn-light btn-lg rounded-pill px-5 mt-3">
            Khám phá ngay
        </a>
    </div>
</div>

<main class="container mb-5">
    <h2 class="section-title">Sản phẩm nổi bật</h2>
    <div class="row g-4">
        <c:choose>
            <c:when test="${not empty featuredProducts}">
                <c:forEach var="product" items="${featuredProducts}">
                    <c:url var="detailUrl" value="/products/${product.id}"/>
                    <div class="col-6 col-md-3">
                        <div class="card product-card h-100 shadow-sm">
                            <img src="${not empty product.imageUrl ? product.imageUrl : fallbackUrl}"
                                 class="card-img-top"
                                 alt="<c:out value='${product.name}'/>">
                            <div class="card-body d-flex flex-column">
                                <h3 class="h6"><c:out value="${product.name}"/></h3>
                                <p class="price mt-auto">
                                    <c:choose>
                                        <c:when test="${not empty product.discountPrice}">
                                            <fmt:formatNumber value="${product.discountPrice}"
                                                              maxFractionDigits="0"/> đ
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${product.price}"
                                                              maxFractionDigits="0"/> đ
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <a href="${detailUrl}" class="btn btn-starshop btn-sm w-100">
                                    Xem chi tiết
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="col-12 text-center py-5">
                    <p class="text-muted">Chưa có sản phẩm bán chạy.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
