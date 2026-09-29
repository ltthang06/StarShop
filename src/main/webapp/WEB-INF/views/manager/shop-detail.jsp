<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Chi tiết cửa hàng - StarShop</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
<body class="bg-light">
<nav class="navbar navbar-dark bg-dark"><div class="container"><a class="navbar-brand" href="${pageContext.request.contextPath}/manager">StarShop Admin</a><a class="btn btn-outline-light btn-sm" href="${pageContext.request.contextPath}/manager/shops">Cửa hàng</a></div></nav>
<main class="container py-4">
    <a href="${pageContext.request.contextPath}/manager/shops" class="text-decoration-none">← Danh sách cửa hàng</a>
    <h1 class="h3 mt-3"><c:out value="${shop.name}"/></h1>
    <c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
    <div class="row g-3"><div class="col-lg-8"><div class="card shadow-sm"><div class="card-body">
        <dl class="row mb-0"><dt class="col-sm-4">Trạng thái</dt><dd class="col-sm-8">${shop.status}</dd><dt class="col-sm-4">Chủ shop</dt><dd class="col-sm-8"><c:out value="${shop.owner.fullName}"/> (<c:out value="${shop.owner.email}"/>)</dd><dt class="col-sm-4">Điện thoại</dt><dd class="col-sm-8"><c:out value="${shop.phone}"/></dd><dt class="col-sm-4">Địa chỉ</dt><dd class="col-sm-8"><c:out value="${shop.address}"/></dd><dt class="col-sm-4">Mô tả</dt><dd class="col-sm-8"><c:out value="${shop.description}"/></dd></dl>
    </div></div></div><div class="col-lg-4"><div class="card shadow-sm"><div class="card-body"><h2 class="h5">Xử lý cửa hàng</h2>
        <c:if test="${shop.status == 'PENDING'}"><form method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/approve" class="mb-2"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-success w-100">Duyệt cửa hàng</button></form><form method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/reject"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-outline-danger w-100">Từ chối</button></form></c:if>
        <c:if test="${shop.status == 'ACTIVE'}"><form method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/block"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-outline-danger w-100">Khóa cửa hàng</button></form></c:if>
        <c:if test="${shop.status == 'BLOCKED'}"><form method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/reopen"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-success w-100">Mở lại cửa hàng</button></form></c:if>
    </div></div></div></div>
    <c:if test="${shop.status == 'ACTIVE'}"><div class="card shadow-sm mt-3"><div class="card-body"><h2 class="h5">Chiết khấu ứng dụng</h2><p class="text-muted">Mức đang áp dụng: <strong>${empty commission ? 'Chưa thiết lập' : commission.ratePercent}</strong><c:if test="${not empty commission}">%</c:if></p><form class="row g-2 align-items-end" method="post" action="${pageContext.request.contextPath}/manager/shops/${shop.id}/commission"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><div class="col-sm-4"><label for="rate" class="form-label">Tỷ lệ (%)</label><input id="rate" class="form-control" name="ratePercent" type="number" min="0" max="100" step="0.01" required></div><div class="col-auto"><button class="btn btn-primary">Lưu chiết khấu</button></div></form></div></div></c:if>
</main></body></html>
