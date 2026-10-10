<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jsp" %>

<fmt:setLocale value="vi_VN"/>
<c:url var="productsUrl" value="/products"/>
<c:url var="fallbackUrl" value="/images/no-image.svg"/>

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
        </div>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
