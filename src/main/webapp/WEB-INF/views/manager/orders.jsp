<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="fragments/header.jsp" %>
<h1 class="h3">Theo dõi và phân công đơn hàng</h1>
<p class="text-muted">Xem thông tin đơn và phân công giao hàng khi cửa hàng đã chuẩn bị xong.</p>
<form method="get" class="row g-2 align-items-end mb-4">
    <div class="col-sm-8 col-lg-5"><label for="order-status" class="form-label">Trạng thái đơn</label>
        <select class="form-select" id="order-status" name="status"><option value="">Mọi trạng thái</option>
            <c:forEach items="${statuses}" var="state"><option value="${state}" ${selectedStatus == state ? 'selected' : ''}>${viewFormat.label(state)}</option></c:forEach>
        </select>
    </div>
    <div class="col-auto"><button class="btn btn-primary">Lọc</button> <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/manager/orders">Đặt lại</a></div>
</form>
<c:forEach items="${orders.content}" var="order">
    <div class="card mb-3 shadow-sm"><div class="card-body p-3 p-md-4">
        <div class="d-flex flex-wrap gap-2 justify-content-between align-items-start mb-2">
            <h2 class="h5 mb-0"><a class="text-decoration-none" href="${pageContext.request.contextPath}/manager/orders/${order.id}">Đơn #${order.id}</a> · <c:out value="${order.shop.name}"/></h2>
            <shop:status value="${order.status}"/>
        </div>
        <p class="text-muted small mb-2">${viewFormat.date(order.createdAt)}</p>
        <p class="mb-1"><c:out value="${order.receiverName}"/> · <c:out value="${order.receiverPhone}"/></p>
        <p class="mb-2"><c:out value="${order.shippingAddress}"/></p>
        <div class="d-flex justify-content-between align-items-center gap-2 flex-wrap mb-3">
            <strong><shop:money value="${order.totalAmount}"/></strong>
            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/manager/orders/${order.id}">Xem chi tiết</a>
        </div>
        <c:if test="${order.status == 'READY_FOR_PICKUP'}">
            <c:choose><c:when test="${empty shippers or empty providers}">
                <div class="alert alert-warning mb-0">Cần có nhân viên giao hàng và đơn vị vận chuyển đang hoạt động trước khi phân công.</div>
            </c:when><c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/manager/orders/${order.id}/assign" class="row g-2 border-top pt-3">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="col-md-4"><label class="form-label" for="shipper-${order.id}">Nhân viên giao hàng</label>
                        <select name="shipperId" id="shipper-${order.id}" class="form-select" required><option value="">Chọn nhân viên</option><c:forEach items="${shippers}" var="s"><option value="${s.id}"><c:out value="${s.fullName}"/> · <c:out value="${s.email}"/></option></c:forEach></select>
                    </div>
                    <div class="col-md-4"><label class="form-label" for="provider-${order.id}">Đơn vị vận chuyển</label>
                        <select name="providerId" id="provider-${order.id}" class="form-select" required><option value="">Chọn đơn vị</option><c:forEach items="${providers}" var="p"><option value="${p.id}"><c:out value="${p.name}"/></option></c:forEach></select>
                    </div>
                    <div class="col-md-4"><label class="form-label" for="note-${order.id}">Ghi chú</label><input id="note-${order.id}" name="note" maxlength="500" class="form-control"></div>
                    <div><button class="btn btn-primary mt-2">Phân công giao hàng</button></div>
                </form>
            </c:otherwise></c:choose>
        </c:if>
    </div></div>
</c:forEach>
<c:if test="${empty orders.content}"><div class="alert alert-secondary">Không có đơn hàng phù hợp.</div></c:if>
<c:set var="paged" value="${orders}"/><%@ include file="fragments/paging.jsp" %>
<%@ include file="fragments/footer.jsp" %>
