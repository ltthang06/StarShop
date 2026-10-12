<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jsp" %>

<fmt:setLocale value="vi_VN"/>
<c:url var="productsUrl" value="/products"/>

<main class="container py-5">
    <h1 class="section-title">Đơn hàng của tôi</h1>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}"/></div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty orders.content}">
            <p class="text-muted">Chưa có đơn hàng nào.</p>
            <a class="btn btn-starshop" href="${productsUrl}">Xem sản phẩm</a>
        </c:when>
        <c:otherwise>
            <c:forEach var="order" items="${orders.content}">
                <c:url var="cancelUrl" value="/orders/${order.id}/cancel"/>
                <c:url var="returnUrl" value="/orders/${order.id}/return"/>
                <div class="card shadow-sm mb-4">
                    <div class="card-header d-flex justify-content-between">
                        <span>
                            Đơn #${order.id} · <c:out value="${order.shopName}"/>
                        </span>
                        <strong><c:out value="${order.status}"/></strong>
                    </div>
                    <div class="card-body">
                        <p class="text-muted mb-2">
                            ${order.createdAt} ·
                            <c:out value="${order.shippingAddress}"/>
                        </p>
                        <c:forEach var="line" items="${order.lines}">
                            <c:url var="reviewUrl"
                                   value="/orders/${order.id}/items/${line.detailId}/review"/>
                            <div class="d-flex justify-content-between border-bottom py-2">
                                <span>
                                    <c:out value="${line.productName}"/>
                                    × ${line.quantity}
                                    <c:if test="${line.canReview}">
                                        <a class="d-block small" href="${reviewUrl}">
                                            Viết đánh giá
                                        </a>
                                    </c:if>
                                </span>
                                <span>
                                    <fmt:formatNumber value="${line.subtotal}"
                                                      maxFractionDigits="0"/> đ
                                </span>
                            </div>
                        </c:forEach>
                        <p class="mt-3 mb-1">Phí giao hàng:
                            <fmt:formatNumber value="${order.shippingFee}"
                                              maxFractionDigits="0"/> đ
                        </p>
                        <p class="mb-3 fw-bold text-danger">Tổng cộng:
                            <fmt:formatNumber value="${order.totalAmount}"
                                              maxFractionDigits="0"/> đ
                        </p>
                        <c:if test="${order.canCancel}">
                            <form action="${cancelUrl}" method="post">
                                <input type="hidden" name="${_csrf.parameterName}"
                                       value="${_csrf.token}">
                                <button class="btn btn-outline-danger btn-sm"
                                        type="submit">Hủy đơn</button>
                            </form>
                        </c:if>
                        <c:if test="${order.canReturn}">
                            <form action="${returnUrl}" method="post">
                                <input type="hidden" name="${_csrf.parameterName}"
                                       value="${_csrf.token}">
                                <button class="btn btn-outline-secondary btn-sm"
                                        type="submit">Yêu cầu trả hàng</button>
                            </form>
                        </c:if>
                    </div>
                </div>
            </c:forEach>

            <nav class="d-flex justify-content-between mt-4">
                <c:if test="${not orders.first}">
                    <c:url var="previousUrl" value="/orders">
                        <c:param name="page" value="${orders.number - 1}"/>
                    </c:url>
                    <a href="${previousUrl}" class="btn btn-outline-danger">
                        Trang trước
                    </a>
                </c:if>
                <span>Trang ${orders.number + 1} / ${orders.totalPages}</span>
                <c:if test="${not orders.last}">
                    <c:url var="nextUrl" value="/orders">
                        <c:param name="page" value="${orders.number + 1}"/>
                    </c:url>
                    <a href="${nextUrl}" class="btn btn-outline-danger">
                        Trang sau
                    </a>
                </c:if>
            </nav>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="../common/footer.jsp" %>
