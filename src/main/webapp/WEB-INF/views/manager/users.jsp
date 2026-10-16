<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="fragments/header.jsp" %>
<h1 class="h3">Quản lý người dùng</h1>
<p class="text-muted">Tìm kiếm tài khoản, xem thông tin và quản lý trạng thái hoạt động theo quyền.</p>
<form method="get" class="row g-2 align-items-end mb-4">
    <div class="col-sm-8 col-md-6"><label for="user-keyword" class="form-label">Họ tên hoặc email</label><input id="user-keyword" class="form-control" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Nhập tên hoặc email"></div>
    <div class="col-auto"><button class="btn btn-primary">Tìm kiếm</button></div>
</form>
<div class="card shadow-sm"><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead><tr><th>Họ tên</th><th>Email</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${users.content}" var="account">
    <c:set var="targetAdmin" value="false"/><c:set var="targetManager" value="false"/>
    <c:forEach items="${account.roles}" var="role">
        <c:if test="${role.name == 'ADMIN'}"><c:set var="targetAdmin" value="true"/></c:if>
        <c:if test="${role.name == 'MANAGER'}"><c:set var="targetManager" value="true"/></c:if>
    </c:forEach>
    <c:set var="self" value="${account.email.equalsIgnoreCase(pageContext.request.userPrincipal.name)}"/>
    <tr><td><a class="text-decoration-none" href="${pageContext.request.contextPath}/manager/users/${account.id}"><c:out value="${account.fullName}"/></a></td>
    <td><c:out value="${account.email}"/></td><td><shop:status value="${account.status}"/></td><td>
        <c:choose>
            <c:when test="${self}"><small class="text-muted">Tài khoản của bạn</small></c:when>
            <c:when test="${targetAdmin or (targetManager and not pageContext.request.isUserInRole('ADMIN'))}"><small class="text-muted">Tài khoản được bảo vệ</small></c:when>
            <c:when test="${account.status == 'ACTIVE' or account.status == 'LOCKED'}">
                <form method="post" action="${pageContext.request.contextPath}/manager/users/${account.id}/lock">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="locked" value="${account.status == 'ACTIVE'}">
                    <button class="btn btn-sm ${account.status == 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-success'}">${account.status == 'ACTIVE' ? 'Khóa tài khoản' : 'Mở khóa'}</button>
                </form>
            </c:when>
            <c:otherwise><small class="text-muted">Chờ người dùng kích hoạt</small></c:otherwise>
        </c:choose>
    </td></tr>
</c:forEach>
<c:if test="${empty users.content}"><tr><td colspan="4" class="text-center text-muted py-4">Không có tài khoản phù hợp.</td></tr></c:if>
</tbody></table></div></div>
<c:set var="paged" value="${users}"/><%@ include file="fragments/paging.jsp" %>
<%@ include file="fragments/footer.jsp" %>
