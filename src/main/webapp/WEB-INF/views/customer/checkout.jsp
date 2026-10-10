<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jsp" %>

<fmt:setLocale value="vi_VN"/>
<c:url var="checkoutUrl" value="/checkout"/>
<c:url var="addressUrl" value="/profile/addresses/new"/>

<main class="container py-5">
    <h1 class="section-title">Đặt hàng</h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
    </c:if>

    <c:if test="${empty addresses}">
        <div class="alert alert-warning">
            Anh cần thêm địa chỉ nhận hàng trước khi đặt.
            <a href="${addressUrl}">Thêm địa chỉ</a>
        </div>
    </c:if>
    <c:if test="${empty providers}">
        <div class="alert alert-warning">
            Hiện chưa có đơn vị vận chuyển đang hoạt động.
        </div>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-8">
            <form action="${checkoutUrl}" method="post">
                <input type="hidden" name="${_csrf.parameterName}"
                       value="${_csrf.token}">

                <div class="card shadow-sm mb-3">
                    <div class="card-body">
                        <h2 class="h5">Địa chỉ nhận hàng</h2>
                        <c:forEach var="address" items="${addresses}">
                            <div class="form-check mb-2">
                                <input class="form-check-input" type="radio"
                                       name="addressId" id="address-${address.id}"
                                       value="${address.id}"
                                       ${address.defaultAddress ? 'checked' : ''} required>
                                <label class="form-check-label" for="address-${address.id}">
                                    <strong><c:out value="${address.recipientName}"/></strong>
                                    · <c:out value="${address.phone}"/><br>
                                    <c:out value="${address.addressLine}"/>
                                    <c:if test="${not empty address.ward}">,
                                        <c:out value="${address.ward}"/>
                                    </c:if>
                                    <c:if test="${not empty address.district}">,
                                        <c:out value="${address.district}"/>
                                    </c:if>
                                    <c:if test="${not empty address.province}">,
                                        <c:out value="${address.province}"/>
                                    </c:if>
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <div class="card shadow-sm mb-3">
                    <div class="card-body">
                        <h2 class="h5">Vận chuyển và thanh toán</h2>
                        <label class="form-label" for="providerId">
                            Đơn vị vận chuyển
                        </label>
                        <select class="form-select mb-3" id="providerId"
                                name="providerId" required>
                            <c:forEach var="provider" items="${providers}">
                                <option value="${provider.id}"
                                        data-fee="${provider.baseFee}">
                                    <c:out value="${provider.name}"/> ·
                                    <fmt:formatNumber value="${provider.baseFee}"
                                                      maxFractionDigits="0"/> đ / shop
                                </option>
                            </c:forEach>
                        </select>
                        <p class="mb-3">Thanh toán khi nhận hàng (COD)</p>
                        <label class="form-label" for="note">Ghi chú</label>
                        <textarea class="form-control" id="note" name="note"
                                  rows="3" maxlength="500"></textarea>
                    </div>
                </div>

                <button class="btn btn-danger" type="submit"
                        ${empty addresses or empty providers ? 'disabled' : ''}>
                    Xác nhận đặt hàng
                </button>
            </form>
        </div>

        <div class="col-lg-4">
            <div class="card shadow-sm">
                <div class="card-body" id="checkoutSummary"
                     data-subtotal="${cart.subtotal}"
                     data-shops="${cart.shopCount}">
                    <h2 class="h5">Tóm tắt đơn hàng</h2>
                    <p>Sản phẩm: ${cart.lines.size()} món</p>
                    <p>Tạm tính:
                        <fmt:formatNumber value="${cart.subtotal}"
                                          maxFractionDigits="0"/> đ
                    </p>
                    <p>Phí giao hàng: <span id="shippingFee">0 đ</span></p>
                    <hr>
                    <p class="fs-5 fw-bold text-danger">
                        Tổng cộng: <span id="checkoutTotal">0 đ</span>
                    </p>
                    <p class="small text-muted mb-0">
                        Mỗi shop tạo một đơn hàng và tính phí giao riêng.
                    </p>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
    const providerSelect = document.getElementById('providerId');
    const checkoutSummary = document.getElementById('checkoutSummary');
    const subtotal = Number(checkoutSummary.dataset.subtotal);
    const shopCount = Number(checkoutSummary.dataset.shops);
    const money = new Intl.NumberFormat('vi-VN');

    function updateCheckoutTotal() {
        const selected = providerSelect.selectedOptions[0];
        const fee = selected ? Number(selected.dataset.fee) * shopCount : 0;
        document.getElementById('shippingFee').textContent = money.format(fee) + ' đ';
        document.getElementById('checkoutTotal').textContent =
            money.format(subtotal + fee) + ' đ';
    }

    providerSelect.addEventListener('change', updateCheckoutTotal);
    updateCheckoutTotal();
</script>

<%@ include file="../common/footer.jsp" %>
