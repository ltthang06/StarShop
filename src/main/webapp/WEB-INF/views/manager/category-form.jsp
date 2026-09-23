<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>${pageTitle} - StarShop</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
<body class="bg-light"><nav class="navbar navbar-dark bg-dark"><div class="container"><a class="navbar-brand" href="${pageContext.request.contextPath}/manager">StarShop Admin</a></div></nav>
<main class="container py-4"><div class="row justify-content-center"><div class="col-lg-7"><div class="card shadow-sm"><div class="card-body p-4"><h1 class="h4 mb-4">${pageTitle}</h1>
<c:if test="${not empty error}"><div class="alert alert-danger">${error}</div></c:if>
<form:form method="post" action="${pageContext.request.contextPath}/manager/categories/save" modelAttribute="categoryForm"><form:hidden path="id"/><div class="mb-3"><form:label path="name" cssClass="form-label">Tên danh mục</form:label><form:input path="name" cssClass="form-control"/><form:errors path="name" cssClass="text-danger small"/></div><div class="mb-3"><form:label path="description" cssClass="form-label">Mô tả</form:label><form:textarea path="description" rows="4" cssClass="form-control"/><form:errors path="description" cssClass="text-danger small"/></div><div class="form-check mb-4"><form:checkbox path="active" cssClass="form-check-input"/><form:label path="active" cssClass="form-check-label">Kích hoạt danh mục</form:label></div><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/manager/categories">Hủy</a><button class="btn btn-primary ms-2">Lưu danh mục</button></form:form>
</div></div></div></div></main></body></html>
