<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jsp" %>

<fmt:setLocale value="vi_VN"/>
<c:url var="productsUrl" value="/products"/>
<c:url var="fallbackUrl" value="/images/no-image.svg"/>

<main class="container py-5">
    <h1 class="section-title">Giỏ hàng</h1>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}"/></div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty cart.lines}">
            <p class="text-muted">Giỏ hàng đang trống.</p>
            <a class="btn btn-starshop" href="${productsUrl}">Xem sản phẩm</a>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <div class="col-lg-8">
                    <c:forEach var="item" items="${cart.lines}">
                        <c:url var="detailUrl" value="/products/${item.productId}"/>
                        <c:url var="updateUrl" value="/cart/items/${item.id}"/>
                        <c:url var="deleteUrl" value="/cart/items/${item.id}/delete"/>
                        <div class="card mb-3 shadow-sm">
                            <div class="card-body d-flex gap-3">
                                <img src="${not empty item.imageUrl ? item.imageUrl : fallbackUrl}"
                                     alt="<c:out value='${item.productName}'/>"
                                     style="width: 96px; height: 96px; object-fit: cover">
                                <div class="flex-grow-1">
                                    <a href="${detailUrl}" class="fw-semibold text-decoration-none">
                                        <c:out value="${item.productName}"/>
                                    </a>
                                    <div class="text-muted">
                                        <fmt:formatNumber value="${item.unitPrice}"
                                                          maxFractionDigits="0"/> đ
                                    </div>
                                    <c:if test="${not item.available}">
                                        <div class="text-danger">Sản phẩm không đủ hàng hoặc đã ngừng bán.</div>
                                    </c:if>
                                    <form action="${updateUrl}" method="post"
                                          class="d-flex align-items-center gap-2 mt-2">
                                        <input type="hidden" name="${_csrf.parameterName}"
                                               value="${_csrf.token}">
                                        <input class="form-control form-control-sm" type="number"
                                               name="quantity" min="1" max="99"
                                               value="${item.quantity}" style="width: 80px">
                                        <button class="btn btn-outline-danger btn-sm"
                                                type="submit">Cập nhật</button>
                                    </form>
                                </div>
                                <div class="text-end">
                                    <strong><fmt:formatNumber value="${item.subtotal}"
                                                              maxFractionDigits="0"/> đ</strong>
                                    <form action="${deleteUrl}" method="post" class="mt-2">
                                        <input type="hidden" name="${_csrf.parameterName}"
                                               value="${_csrf.token}">
                                        <button class="btn btn-link text-danger btn-sm p-0"
                                                type="submit">Xóa</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
                <div class="col-lg-4">
                    <div class="card shadow-sm">
                        <div class="card-body">
                            <h2 class="h5">Tạm tính</h2>
                            <p class="fs-4 fw-bold text-danger">
                                <fmt:formatNumber value="${cart.subtotal}"
                                                  maxFractionDigits="0"/> đ
                            </p>
                            <c:if test="${not cart.readyToCheckout}">
                                <p class="text-danger mb-0">
                                    Hãy cập nhật hoặc xóa sản phẩm không còn đủ hàng.
                                </p>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="../common/footer.jsp" %>
