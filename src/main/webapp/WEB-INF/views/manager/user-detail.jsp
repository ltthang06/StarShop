<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ include file="fragments/header.jsp" %>
<h1 class="h3">Chi tiết tài khoản #${account.id}</h1><div class="card card-body"><dl class="row">
<dt class="col-sm-3">Họ tên</dt><dd class="col-sm-9"><c:out value="${account.fullName}"/></dd>
<dt class="col-sm-3">Email</dt><dd class="col-sm-9"><c:out value="${account.email}"/></dd>
<dt class="col-sm-3">Điện thoại</dt><dd class="col-sm-9"><c:out value="${account.phone}"/></dd>
<dt class="col-sm-3">Trạng thái</dt><dd class="col-sm-9"><shop:status value="${account.status}"/></dd>
<dt class="col-sm-3">Ngày tạo</dt><dd class="col-sm-9">${viewFormat.date(account.createdAt)}</dd>
<dt class="col-sm-3">Vai trò</dt><dd class="col-sm-9"><c:forEach items="${account.roles}" var="r"><span class="badge bg-secondary">${viewFormat.label(r.name)}</span> </c:forEach></dd>
</dl><c:if test="${pageContext.request.isUserInRole('ADMIN')}"><h2 class="h5">Cấp / gỡ vai trò vận hành</h2><form method="post" action="${pageContext.request.contextPath}/manager/users/${account.id}/roles" class="row g-2">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><div class="col-md-4"><select class="form-select" name="role"><option value="MANAGER">Quản lý</option><option value="SHIPPER">Nhân viên giao hàng</option></select></div><div class="col-md-4"><select class="form-select" name="enabled"><option value="true">Cấp quyền</option><option value="false">Gỡ quyền</option></select></div><div class="col-md-4"><button class="btn btn-primary">Cập nhật</button></div></form></c:if></div>
<%@ include file="fragments/footer.jsp" %>
