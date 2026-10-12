<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/header.jsp" %>

    <div class="d-flex justify-content-between align-items-center mb-3"><h1 class="h3 mb-0">Quản lý danh mục</h1><a class="btn btn-primary" href="${pageContext.request.contextPath}/manager/categories/new">+ Thêm danh mục</a></div>

    <form class="row g-2 mb-3" method="get"><div class="col-sm-8 col-md-5"><input class="form-control" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Tìm theo tên danh mục"></div><div class="col-auto"><button class="btn btn-outline-secondary">Tìm kiếm</button></div></form>
    <div class="card shadow-sm"><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-dark"><tr><th>#</th><th>Tên</th><th>Mô tả</th><th>Trạng thái</th><th class="text-end">Thao tác</th></tr></thead><tbody>
    <c:choose><c:when test="${empty categories}"><tr><td colspan="5" class="text-center text-muted py-4">Chưa có danh mục</td></tr></c:when><c:otherwise><c:forEach var="category" items="${categories}" varStatus="loop"><tr><td>${loop.count}</td><td class="fw-semibold"><c:out value="${category.name}"/></td><td><c:out value="${category.description}"/></td><td><span class="badge ${category.active ? 'text-bg-success' : 'text-bg-secondary'}">${category.active ? 'Đang bật' : 'Đang tắt'}</span></td><td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/manager/categories/${category.id}/edit">Sửa</a><form class="d-inline" method="post" action="${pageContext.request.contextPath}/manager/categories/${category.id}/toggle"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm btn-outline-secondary">${category.active ? 'Tắt' : 'Bật'}</button></form></td></tr></c:forEach></c:otherwise></c:choose>
    </tbody></table></div></div>
<%@ include file="fragments/footer.jsp" %>
