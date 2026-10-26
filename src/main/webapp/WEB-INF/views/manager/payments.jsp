<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="fragments/header.jsp" %>
<h1 class="h3">Đối soát thanh toán VNPAY</h1>
<p class="text-muted">Các giao dịch chưa có kết quả cuối cùng. Chỉ hoàn tồn kho khi xác định đơn chưa mở cổng thanh toán hoặc VNPAY xác nhận thất bại.</p>
<c:if test="${empty payments.content}"><div class="alert alert-success">Không có giao dịch đang chờ đối soát.</div></c:if>
<c:forEach items="${payments.content}" var="payment">
<div class="card shadow-sm mb-3"><div class="card-body">
<h2 class="h6 text-break"><c:out value="${payment.reference}"/></h2>
<p>Đơn: <c:forEach items="${payment.orders}" var="id"><a href="${pageContext.request.contextPath}/manager/orders/${id}">#${id}</a> </c:forEach></p>
<p><strong><shop:money value="${payment.amount}"/></strong> · <c:out value="${payment.state}"/></p>
<p>Hết hạn: ${viewFormat.date(payment.expiresAt)} · Kiểm tra gần nhất: ${viewFormat.date(payment.lastCheckedAt)}</p>
<c:if test="${not empty payment.note}"><div class="alert alert-warning"><c:out value="${payment.note}"/></div></c:if>
<c:choose><c:when test="${payment.legacy}"><p class="text-muted">Giao dịch thuộc cấu hình cũ hoặc chưa xác định Merchant. Cần đối chiếu với VNPAY; không hủy tự động.</p></c:when><c:otherwise>
<form method="post" action="${pageContext.request.contextPath}/manager/payments/${payment.reference}/reconcile">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-outline-primary">Đối soát với VNPAY</button>
</form></c:otherwise></c:choose>
</div></div>
</c:forEach>
<div class="d-flex gap-2"><c:if test="${payments.hasPrevious()}"><a class="btn btn-outline-secondary" href="?page=${payments.number - 1}">Trang trước</a></c:if><c:if test="${payments.hasNext()}"><a class="btn btn-outline-secondary" href="?page=${payments.number + 1}">Trang sau</a></c:if></div>
<%@ include file="fragments/footer.jsp" %>
