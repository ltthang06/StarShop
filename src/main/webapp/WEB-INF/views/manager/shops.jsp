<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Cửa hàng - StarShop</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>
<body class="bg-light">
<nav class="navbar navbar-dark bg-dark"><div class="container"><a class="navbar-brand" href="${pageContext.request.contextPath}/manager">StarShop Admin</a><a class="btn btn-outline-light btn-sm" href="${pageContext.request.contextPath}/manager">Dashboard</a></div></nav>
<main class="container py-4">
    <h1 class="h3 mb-3">Quản lý cửa hàng</h1>
    <form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/manager/shops">
        <div class="col-md-5"><input class="form-control" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Tên cửa hàng hoặc email chủ shop"></div>
        <div class="col-md-3"><select class="form-select" name="status"><option value="">Mọi trạng thái</option><c:forEach items="${statuses}" var="item"><option value="${item}" ${selectedStatus == item ? 'selected' : ''}>${item}</option></c:forEach></select></div>
        <div class="col-auto"><button class="btn btn-primary">Tìm kiếm</button></div>
    </form>
    <div class="card shadow-sm"><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-dark"><tr><th>Cửa hàng</th><th>Chủ shop</th><th>Ngày đăng ký</th><th>Trạng thái</th><th></th></tr></thead><tbody>
    <c:choose><c:when test="${shops.empty}"><tr><td colspan="5" class="text-center text-muted py-4">Không có cửa hàng phù hợp</td></tr></c:when><c:otherwise>
    <c:forEach items="${shops.content}" var="shop"><tr><td><c:out value="${shop.name}"/></td><td><c:out value="${shop.owner.email}"/></td><td>${shop.createdAt}</td><td><span class="badge ${shop.status == 'ACTIVE' ? 'text-bg-success' : shop.status == 'PENDING' ? 'text-bg-warning' : 'text-bg-secondary'}">${shop.status}</span></td><td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/manager/shops/${shop.id}">Chi tiết</a></td></tr></c:forEach>
    </c:otherwise></c:choose></tbody></table></div></div>
    <c:if test="${shops.totalPages > 1}"><nav class="mt-3" aria-label="Phân trang"><ul class="pagination"><c:if test="${shops.hasPrevious()}"><c:url var="previous" value="/manager/shops"><c:param name="page" value="${shops.number - 1}"/><c:param name="keyword" value="${keyword}"/><c:param name="status" value="${selectedStatus}"/></c:url><li class="page-item"><a class="page-link" href="${previous}">Trước</a></li></c:if><li class="page-item disabled"><span class="page-link">${shops.number + 1} / ${shops.totalPages}</span></li><c:if test="${shops.hasNext()}"><c:url var="next" value="/manager/shops"><c:param name="page" value="${shops.number + 1}"/><c:param name="keyword" value="${keyword}"/><c:param name="status" value="${selectedStatus}"/></c:url><li class="page-item"><a class="page-link" href="${next}">Sau</a></li></c:if></ul></nav></c:if>
</main></body></html>
