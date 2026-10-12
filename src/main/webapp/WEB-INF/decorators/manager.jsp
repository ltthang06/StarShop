<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><title><sitemesh:write property="title"/></title><sitemesh:write property="head"/></head>
<body class="bg-light"><sitemesh:write property="body"/>
<footer id="manager-layout-footer" class="container py-3 border-top d-flex flex-wrap gap-3 justify-content-between">
<span class="text-muted">StarShop · <c:out value="${authenticatedEmail}"/></span>
<form method="post" action="${pageContext.request.contextPath}/logout"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm btn-outline-secondary">Đăng xuất</button></form>
</footer></body></html>
