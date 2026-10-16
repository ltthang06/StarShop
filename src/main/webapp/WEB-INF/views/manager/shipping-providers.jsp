<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ include file="fragments/header.jsp" %>

<h1 class="h3 mb-3">Đơn vị vận chuyển</h1><div class="row g-4"><div class="col-lg-5"><div class="card p-3"><h2 class="h5">${empty providerForm.id ? 'Thêm đơn vị' : 'Sửa đơn vị'}</h2>
<form:form modelAttribute="providerForm" method="post" action="${pageContext.request.contextPath}/manager/shipping-providers"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><form:hidden path="id"/><form:errors path="*" cssClass="text-danger d-block"/>
<label class="form-label">Tên đơn vị</label><form:input path="name" cssClass="form-control mb-3" maxlength="150"/>
<label class="form-label">Điện thoại</label><form:input path="phone" cssClass="form-control mb-3" maxlength="20"/>
<label class="form-label">Phí cơ bản (VND)</label><form:input path="baseFee" type="number" min="0" step="0.01" cssClass="form-control mb-3"/>
<label class="form-label">Số ngày dự kiến</label><form:input path="estimatedDays" type="number" min="1" max="365" cssClass="form-control mb-3"/>
<div class="form-check mb-3"><form:checkbox path="active" cssClass="form-check-input"/><label class="form-check-label">Đang hoạt động</label></div><button class="btn btn-primary">Lưu</button> <a href="${pageContext.request.contextPath}/manager/shipping-providers" class="btn btn-outline-secondary">Làm mới</a></form:form></div></div>
<div class="col-lg-7"><div class="table-responsive bg-white rounded"><table class="table"><thead><tr><th>Tên</th><th>Phí</th><th>Ngày</th><th>Trạng thái</th><th></th></tr></thead><tbody><c:forEach items="${providers}" var="provider"><tr><td><c:out value="${provider.name}"/></td><td><shop:money value="${provider.baseFee}"/></td><td>${provider.estimatedDays}</td><td>${provider.active ? 'Đang bật' : 'Đã tắt'}</td><td><a href="${pageContext.request.contextPath}/manager/shipping-providers?edit=${provider.id}">Sửa</a></td></tr></c:forEach></tbody></table></div></div></div><%@ include file="fragments/footer.jsp" %>
