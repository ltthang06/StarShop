<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="shop" tagdir="/WEB-INF/tags" %>
<%@ include file="../common/header.jsp" %>
<main class="container py-5" id="vnpay-payment" data-state="${paymentState}" data-status-url="${pageContext.request.contextPath}/payments/${reference}/status"><div class="card shadow-sm mx-auto" style="max-width:720px"><div class="card-body p-4">
<h1 class="h3">Thanh toán VNPAY</h1>
<p class="small text-muted">VNPAY Sandbox: môi trường kiểm thử, không dùng để thanh toán tiền thật.</p>
<c:if test="${not empty errorMessage}"><div class="alert alert-danger" role="alert"><c:out value="${errorMessage}"/><c:if test="${canCheck}"><form method="post" action="${pageContext.request.contextPath}/payments/${reference}/reconcile">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-outline-primary">Kiểm tra với VNPAY</button></form></c:if>
</div></c:if>
<c:if test="${not empty reference}">
<p class="text-muted text-break">Mã giao dịch: <c:out value="${reference}"/></p>
<p class="fs-4 fw-bold text-danger"><shop:money value="${amount}"/></p>
<p>Đơn hàng: <c:forEach items="${orderIds}" var="id">#${id} </c:forEach></p>
<c:choose>
<c:when test="${paymentState == 'PAID'}"><div class="alert alert-success" role="status">Đã thanh toán thành công. Cửa hàng sẽ tiếp tục xử lý đơn của bạn.</div></c:when>
<c:when test="${paymentState == 'FAILED'}"><div class="alert alert-warning">Giao dịch đã kết thúc và đơn đã được hủy. Số lượng sản phẩm đã được hoàn lại; nếu bạn đã trả tiền, vui lòng liên hệ cửa hàng để đối chiếu.</div></c:when>
<c:when test="${paymentState == 'REVIEW_REQUIRED'}"><div class="alert alert-warning">Giao dịch cần được kiểm tra với VNPAY. Vui lòng giữ mã giao dịch và liên hệ cửa hàng, không thanh toán thêm lần nữa.</div></c:when>
<c:otherwise><div class="alert alert-info" role="status"><c:choose>
<c:when test="${reportedSuccess}">Đã nhận kết quả thành công từ VNPAY, đang chờ xác nhận thanh toán. Không thanh toán hoặc đặt đơn thêm lần nữa.</c:when>
<c:when test="${returned}">Đã nhận kết quả từ VNPAY, đang xác nhận trạng thái cuối cùng. Không thanh toán lại trong lúc chờ.</c:when>
<c:when test="${expired}">Đã hết thời gian mở thanh toán. Hệ thống đang đối soát trước khi xử lý đơn và tồn kho. Nếu đã trả tiền, vui lòng giữ mã giao dịch và không đặt lại đơn.</c:when>
<c:otherwise>Đang chờ thanh toán. Nếu đã xác nhận trả tiền trên VNPAY, hãy kiểm tra trạng thái và không thanh toán thêm lần nữa.</c:otherwise>
</c:choose></div><p>Thời hạn thanh toán: <c:out value="${expiresAt}"/></p></c:otherwise>
</c:choose>
<c:if test="${not empty returnNotice}"><p class="text-muted"><c:out value="${returnNotice}"/></p></c:if>
<c:if test="${not empty checkNotice}"><p role="status"><c:out value="${checkNotice}"/></p></c:if>
<div class="d-flex flex-wrap gap-2 mt-3">
<c:if test="${canPay}"><a class="btn btn-danger" href="${pageContext.request.contextPath}/payments/${reference}/redirect">Tiếp tục đến VNPAY</a></c:if>
<a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/payments/${reference}">Cập nhật trạng thái</a>
<c:if test="${canCheck}"><form method="post" action="${pageContext.request.contextPath}/payments/${reference}/reconcile">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-outline-primary">Kiểm tra với VNPAY</button></form></c:if>
</div></c:if>
<a class="d-inline-block mt-4" href="${pageContext.request.contextPath}/orders">Xem lịch sử đơn hàng</a>
</div></div></main>
<c:if test="${paymentState == 'PENDING'}"><script src="${pageContext.request.contextPath}/js/payment-status.js" defer></script></c:if>
<%@ include file="../common/footer.jsp" %>
