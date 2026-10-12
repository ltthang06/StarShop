<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jsp" %>

<fmt:setLocale value="vi_VN"/>
<c:url var="fallbackUrl" value="/images/no-image.svg"/>

<main class="container py-5">
    <h1 class="section-title"><c:out value="${pageTitle}"/></h1>

    <c:choose>
        <c:when test="${empty products}">
            <p class="text-muted">Chưa có sản phẩm nào.</p>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <c:forEach var="product" items="${products}">
                    <c:url var="detailUrl" value="/products/${product.id}"/>
                    <c:url var="removeUrl" value="/wishlist/${product.id}/delete"/>
                    <div class="col-6 col-md-3">
                        <div class="card product-card h-100 shadow-sm">
                            <img src="${not empty product.imageUrl ? product.imageUrl : fallbackUrl}"
                                 class="card-img-top"
                                 alt="<c:out value='${product.name}'/>">
                            <div class="card-body d-flex flex-column">
                                <small class="text-muted">
                                    <c:out value="${product.categoryName}"/>
                                </small>
                                <h2 class="h6 mt-2">
                                    <c:out value="${product.name}"/>
                                </h2>
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
                                <a class="btn btn-starshop btn-sm" href="${detailUrl}">
                                    Xem chi tiết
                                </a>
                                <c:if test="${wishlistPage}">
                                    <form action="${removeUrl}" method="post" class="mt-2">
                                        <input type="hidden" name="${_csrf.parameterName}"
                                               value="${_csrf.token}">
                                        <button class="btn btn-outline-secondary btn-sm w-100"
                                                type="submit">Bỏ yêu thích</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="../common/footer.jsp" %>
