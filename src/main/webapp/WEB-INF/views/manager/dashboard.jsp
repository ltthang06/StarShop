<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ include file="fragments/header.jsp" %>
<h1 class="h3">${managementAdmin ? 'Quản trị hệ thống' : 'Quản lý chi nhánh'}</h1><p>${managementAdmin ? 'Đơn và doanh thu toàn hệ thống từ đầu tháng đến hôm nay.' : 'Đơn, khách đặt hàng và doanh thu của các cửa hàng được phân công từ đầu tháng đến hôm nay.'}</p><div class="row g-3">
<div class="col-md-4"><div class="card card-body shadow-sm">${managementAdmin ? 'Người dùng' : 'Khách đặt hàng trong tháng'}<strong class="fs-3"><fmt:formatNumber value="${report.users()}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">${managementAdmin ? 'Shop' : 'Shop được phân công'}<strong class="fs-3"><fmt:formatNumber value="${report.shops()}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Sản phẩm<strong class="fs-3"><fmt:formatNumber value="${report.products()}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Đơn tháng này<strong class="fs-3"><fmt:formatNumber value="${report.orders()}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Doanh thu đã giao (VND)<strong class="fs-3"><shop:money value="${report.deliveredRevenue()}"/></strong></div></div>
<c:if test="${managementAdmin}"><div class="col-md-4"><div class="card card-body shadow-sm">Shop chờ duyệt<strong class="fs-3"><fmt:formatNumber value="${pendingShopCount}"/></strong></div></div>
<div class="col-md-4"><div class="card card-body shadow-sm">Danh mục<strong class="fs-3"><fmt:formatNumber value="${categoryCount}"/></strong></div></div></c:if></div><a class="btn btn-primary mt-4" href="${pageContext.request.contextPath}/manager/reports">Xem / Xuất báo cáo</a>
<%@ include file="fragments/footer.jsp" %>
