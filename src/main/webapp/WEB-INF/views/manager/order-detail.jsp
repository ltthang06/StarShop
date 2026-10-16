<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="fragments/header.jsp" %>
<c:set var="order" value="${detail.order()}"/>
<a href="${pageContext.request.contextPath}/manager/orders" class="text-decoration-none">← Danh sách đơn hàng</a>
<div class="d-flex flex-wrap gap-2 justify-content-between align-items-center my-3"><h1 class="h3 mb-0">Chi tiết đơn #${order.id}</h1><shop:status value="${order.status}"/></div>
<p class="text-muted">Đặt lúc ${viewFormat.date(order.createdAt)}</p>
<div class="row g-3 mb-4">
    <div class="col-lg-6"><section class="card card-body h-100"><h2 class="h5">Người mua và nhận hàng</h2>
        <dl class="row mb-0"><dt class="col-sm-4">Tài khoản mua</dt><dd class="col-sm-8"><c:out value="${order.user.email}"/></dd>
        <dt class="col-sm-4">Người nhận</dt><dd class="col-sm-8"><c:out value="${order.receiverName}"/></dd>
        <dt class="col-sm-4">Điện thoại</dt><dd class="col-sm-8"><c:out value="${order.receiverPhone}"/></dd>
        <dt class="col-sm-4">Địa chỉ</dt><dd class="col-sm-8"><c:out value="${order.shippingAddress}"/></dd>
        <dt class="col-sm-4">Ghi chú</dt><dd class="col-sm-8"><c:out value="${empty order.note ? 'Không có' : order.note}"/></dd></dl>
    </section></div>
    <div class="col-lg-6"><section class="card card-body h-100"><h2 class="h5">Cửa hàng và giao hàng</h2>
        <dl class="row mb-0"><dt class="col-sm-4">Cửa hàng</dt><dd class="col-sm-8"><a href="${pageContext.request.contextPath}/manager/shops/${order.shop.id}"><c:out value="${order.shop.name}"/></a></dd>
        <dt class="col-sm-4">Đơn vị giao</dt><dd class="col-sm-8"><c:out value="${empty order.shippingProvider ? 'Chưa chọn' : order.shippingProvider.name}"/></dd>
        <dt class="col-sm-4">Nhân viên giao</dt><dd class="col-sm-8"><c:out value="${empty detail.shipperName() ? 'Chưa phân công' : detail.shipperName()}"/><c:if test="${not empty detail.shipperEmail()}"><br><c:out value="${detail.shipperEmail()}"/></c:if></dd>
        <dt class="col-sm-4">Lúc phân công</dt><dd class="col-sm-8">${viewFormat.date(detail.assignedAt())}</dd></dl>
    </section></div>
</div>
<section class="card card-body mb-4"><h2 class="h5">Sản phẩm trong đơn</h2>
    <div class="table-responsive"><table class="table align-middle"><thead><tr><th>Sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead><tbody>
        <c:forEach items="${detail.lines()}" var="line"><tr><td><a href="${pageContext.request.contextPath}/manager/products/${line.productId()}"><c:out value="${line.productName()}"/></a></td><td>${line.quantity()}</td><td><shop:money value="${line.unitPrice()}"/></td><td><shop:money value="${line.subtotal()}"/></td></tr></c:forEach>
        <c:if test="${empty detail.lines()}"><tr><td colspan="4" class="text-muted">Đơn này chưa có chi tiết sản phẩm.</td></tr></c:if>
    </tbody></table></div>
</section>
<div class="row justify-content-end"><div class="col-lg-6"><section class="card card-body"><h2 class="h5">Thanh toán</h2>
    <dl class="row mb-0"><dt class="col-6">Tiền sản phẩm</dt><dd class="col-6 text-end"><shop:money value="${order.subtotal}"/></dd>
    <dt class="col-6">Phí vận chuyển</dt><dd class="col-6 text-end"><shop:money value="${order.shippingFee}"/></dd>
    <dt class="col-6">Giảm giá</dt><dd class="col-6 text-end"><shop:money value="${order.discountAmount}"/></dd>
    <dt class="col-6 fs-5">Tổng thanh toán</dt><dd class="col-6 text-end fs-5 fw-bold"><shop:money value="${order.totalAmount}"/></dd>
    <dt class="col-6">Phương thức</dt><dd class="col-6 text-end">${viewFormat.label(order.paymentMethod)}</dd>
    <dt class="col-6">Trạng thái</dt><dd class="col-6 text-end"><shop:status value="${order.paymentStatus}"/></dd></dl>
</section></div></div>
<%@ include file="fragments/footer.jsp" %>
