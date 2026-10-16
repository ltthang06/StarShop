<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ include file="fragments/header.jsp" %>
<h1 class="h3">Bảng điều khiển Manager / Admin</h1><p>Đơn và doanh thu từ đầu tháng đến hôm nay.</p><div class="row g-3">
<div class="col-md-4"><div class="card card-body shadow-sm">Người dùng<strong class="fs-3"><fmt:formatNumber value="${report.users()}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Shop<strong class="fs-3"><fmt:formatNumber value="${report.shops()}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Sản phẩm<strong class="fs-3"><fmt:formatNumber value="${report.products()}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Đơn tháng này<strong class="fs-3"><fmt:formatNumber value="${report.orders()}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Doanh thu đã giao (VND)<strong class="fs-3"><shop:money value="${report.deliveredRevenue()}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Shop chờ duyệt<strong class="fs-3"><fmt:formatNumber value="${pendingShopCount}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Danh mục<strong class="fs-3"><fmt:formatNumber value="${categoryCount}"/></strong></div></div></div><a class="btn btn-primary mt-4" href="${pageContext.request.contextPath}/manager/reports">Xem / Xuất báo cáo</a>
<%@ include file="fragments/footer.jsp" %>
