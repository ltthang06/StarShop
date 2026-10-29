<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/header.jsp" %>

    <a href="${pageContext.request.contextPath}/manager/shops" class="text-decoration-none">← Danh sách cửa hàng</a>
    <h1 class="h3 mt-3"><c:out value="${shop.name}"/></h1>


    <div class="row g-3"><div class="col-lg-8"><div class="card shadow-sm"><div class="card-body">
        <dl class="row mb-0"><dt class="col-sm-4">Trạng thái</dt><dd class="col-sm-8"><shop:status value="${shop.status}"/></dd><dt class="col-sm-4">Chủ shop</dt><dd class="col-sm-8"><c:out value="${shop.owner.fullName}"/> (<c:out value="${shop.owner.email}"/>)</dd><dt class="col-sm-4">Điện thoại</dt><dd class="col-sm-8"><c:out value="${shop.phone}"/></dd><dt class="col-sm-4">Địa chỉ</dt><dd class="col-sm-8"><c:out value="${shop.address}"/></dd><dt class="col-sm-4">Mô tả</dt><dd class="col-sm-8"><c:out value="${shop.description}"/></dd></dl>
    </div></div></div><div class="col-lg-4"><div class="card shadow-sm"><div class="card-body"><h2 class="h5">Xử lý cửa hàng</h2>
        <c:if test="${shop.status == 'PENDING'}"><c:if test="${empty shop.manager and pageContext.request.isUserInRole('ADMIN')}"><div class="alert alert-warning small">Hãy phân công Manager ở mục bên dưới trước khi duyệt cửa hàng.</div></c:if><form method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/approve" class="mb-2"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-success w-100">Duyệt cửa hàng</button></form><form method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/reject"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-outline-danger w-100">Từ chối</button></form></c:if>
        <c:if test="${shop.status == 'ACTIVE'}"><form method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/block"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-outline-danger w-100">Khóa cửa hàng</button></form></c:if>
        <c:if test="${shop.status == 'BLOCKED'}"><form method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/reopen"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-success w-100">Mở lại cửa hàng</button></form></c:if>
    </div></div></div></div>
    <c:if test="${shop.status == 'ACTIVE'}"><div class="card shadow-sm mt-3"><div class="card-body"><h2 class="h5">Chiết khấu ứng dụng</h2><p class="text-muted">Mức đang áp dụng: <strong>${empty commission ? 'Chưa thiết lập' : commission.ratePercent}</strong><c:if test="${not empty commission}">%</c:if></p><form class="row g-2 align-items-end" method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/commission"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><div class="col-sm-4"><label for="rate" class="form-label">Tỷ lệ (%)</label><input id="rate" class="form-control" name="ratePercent" type="number" min="0" max="100" step="0.01" required></div><div class="col-auto"><button class="btn btn-primary">Lưu chiết khấu</button></div></form></div></div></c:if>
<div class="card mt-3"><div class="card-body"><h2 class="h5">Manager phụ trách cửa hàng</h2>
<p><c:choose><c:when test="${empty shop.manager}">Chưa phân công</c:when><c:otherwise><c:out value="${shop.manager.fullName}"/> — <c:out value="${shop.manager.email}"/></c:otherwise></c:choose></p>
<c:if test="${pageContext.request.isUserInRole('ADMIN')}">
<form method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/manager" class="row g-2 align-items-end">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
<div class="col-sm-8"><label for="shop-manager" class="form-label">Chọn Manager</label><select id="shop-manager" name="managerId" class="form-select"><option value="">Chưa phân công</option>
<c:forEach items="${managerCandidates}" var="candidate"><option value="${candidate.id}" ${shop.manager.id == candidate.id ? 'selected' : ''}><c:out value="${candidate.fullName}"/> — <c:out value="${candidate.email}"/></option></c:forEach>
</select></div><div class="col-auto"><button class="btn btn-primary">Lưu phân công</button></div></form>
<p class="text-muted small mt-2 mb-0">Mỗi cửa hàng được giao cho một Manager. Khi có cửa hàng mới, Admin chọn Manager phụ trách tại đây; Manager chỉ xem và xử lý cửa hàng được giao. Admin xem toàn hệ thống.</p>
</c:if></div></div>
<%@ include file="fragments/footer.jsp" %>
