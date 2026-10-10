<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jsp" %>

<fmt:setLocale value="vi_VN"/>
<c:url var="productsUrl" value="/products"/>
<c:url var="fallbackUrl" value="/images/no-image.svg"/>
<c:url var="wishlistAddUrl" value="/wishlist/${product.id}"/>
<c:url var="cartAddUrl" value="/cart/items"/>

<main class="container py-5">
    <a href="${productsUrl}" class="btn btn-link mb-3">← Tất cả sản phẩm</a>

    <div class="row g-4">
        <div class="col-md-6">
            <img src="${not empty product.imageUrl ? product.imageUrl : fallbackUrl}"
                 class="img-fluid rounded shadow-sm w-100"
                 alt="<c:out value='${product.name}'/>">
        </div>
        <div class="col-md-6">
            <p class="text-muted mb-2">
                <c:out value="${product.categoryName}"/>
            </p>
            <h1><c:out value="${product.name}"/></h1>
            <p class="price fs-3">
                <c:choose>
                    <c:when test="${not empty product.discountPrice}">
                        <fmt:formatNumber value="${product.discountPrice}"
                                          maxFractionDigits="0"/> đ
                        <span class="old-price">
                            <fmt:formatNumber value="${product.price}"
                                              maxFractionDigits="0"/> đ
                        </span>
                    </c:when>
                    <c:otherwise>
                        <fmt:formatNumber value="${product.price}"
                                          maxFractionDigits="0"/> đ
                    </c:otherwise>
                </c:choose>
            </p>
            <p>Đã bán: ${product.soldCount} · Đánh giá: ${product.rating}/5</p>
            <p style="white-space: pre-line">
                <c:out value="${product.description}"/>
            </p>
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success"><c:out value="${successMessage}"/></div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
            </c:if>
            <form action="${cartAddUrl}" method="post" class="d-flex gap-2 mb-3">
                <input type="hidden" name="${_csrf.parameterName}"
                       value="${_csrf.token}">
                <input type="hidden" name="productId" value="${product.id}">
                <input class="form-control" type="number" name="quantity"
                       min="1" max="${product.quantity}" value="1"
                       style="width: 90px" aria-label="Số lượng">
                <button class="btn btn-danger" type="submit">Thêm vào giỏ hàng</button>
            </form>
            <form action="${wishlistAddUrl}" method="post">
                <input type="hidden" name="${_csrf.parameterName}"
                       value="${_csrf.token}">
                <button class="btn btn-outline-danger" type="submit">
                    <i class="bi bi-heart"></i> Thêm vào yêu thích
                </button>
            </form>
        </div>
    </div>

    <section class="mt-5">
        <h2 class="section-title">Đánh giá của khách hàng</h2>
        <c:choose>
            <c:when test="${empty reviews}">
                <p class="text-muted">Sản phẩm chưa có đánh giá.</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="review" items="${reviews}">
                    <div class="card mb-3 shadow-sm">
                        <div class="card-body">
                            <div class="d-flex justify-content-between mb-2">
                                <strong><c:out value="${review.author}"/></strong>
                                <span class="text-muted">${review.createdAt}</span>
                            </div>
                            <p class="text-warning mb-2">${review.rating}/5 sao</p>
                            <p style="white-space: pre-line">
                                <c:out value="${review.content}"/>
                            </p>
                            <c:if test="${not empty review.imageUrl}">
                                <img class="img-fluid rounded mb-2"
                                     src="<c:out value='${review.imageUrl}'/>"
                                     alt="Ảnh đánh giá" style="max-height: 300px">
                            </c:if>
                            <c:if test="${not empty review.videoUrl}">
                                <video class="w-100" controls style="max-height: 360px">
                                    <source src="<c:out value='${review.videoUrl}'/>"
                                            type="video/mp4">
                                </video>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<%@ include file="../common/footer.jsp" %>
