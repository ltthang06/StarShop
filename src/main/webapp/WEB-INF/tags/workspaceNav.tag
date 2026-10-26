<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="navbar navbar-expand-lg navbar-dark starshop-workspace" aria-label="Điều hướng StarShop">
  <div class="container flex-wrap gap-2">
    <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">StarShop</a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#workspace-nav" aria-controls="workspace-nav" aria-expanded="false" aria-label="Mở menu"><span class="navbar-toggler-icon"></span></button>
    <div class="collapse navbar-collapse" id="workspace-nav">
      <div class="navbar-nav me-auto">
        <a class="nav-link" href="${pageContext.request.contextPath}/products">Mua hoa</a>
        <c:if test="${pageContext.request.isUserInRole('USER') or pageContext.request.isUserInRole('VENDOR') or pageContext.request.isUserInRole('ADMIN')}"><a class="nav-link" href="${pageContext.request.contextPath}/vendor">Cửa hàng của tôi</a></c:if>
        <c:if test="${pageContext.request.isUserInRole('MANAGER') or pageContext.request.isUserInRole('ADMIN')}"><a class="nav-link" href="${pageContext.request.contextPath}/manager">Quản trị</a></c:if>
        <c:if test="${pageContext.request.isUserInRole('SHIPPER') or pageContext.request.isUserInRole('ADMIN')}"><a class="nav-link" href="${pageContext.request.contextPath}/shipper">Giao hàng</a></c:if>
        <a class="nav-link" href="${pageContext.request.contextPath}/orders">Đơn mua</a>
        <a class="nav-link" href="${pageContext.request.contextPath}/notifications">Thông báo</a>
      </div>
      <c:if test="${not empty pageContext.request.userPrincipal}"><form method="post" action="${pageContext.request.contextPath}/logout"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm btn-outline-light">Đăng xuất</button></form></c:if>
    </div>
  </div>
</nav>
