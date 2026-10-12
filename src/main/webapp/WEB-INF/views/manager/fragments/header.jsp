<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>StarShop Quản trị</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head><body class="bg-light">
<nav class="navbar navbar-dark bg-dark"><div class="container gap-3 flex-wrap"><a class="navbar-brand" href="${pageContext.request.contextPath}/manager">StarShop Admin</a><div class="d-flex gap-3 flex-wrap">
<a class="text-white" href="${pageContext.request.contextPath}/manager/users">User</a>
<a class="text-white" href="${pageContext.request.contextPath}/manager/shops">Shop</a>
<a class="text-white" href="${pageContext.request.contextPath}/manager/categories">Danh mục</a>
<a class="text-white" href="${pageContext.request.contextPath}/manager/products">Sản phẩm</a>
<a class="text-white" href="${pageContext.request.contextPath}/manager/promotions">Voucher</a>
<a class="text-white" href="${pageContext.request.contextPath}/manager/shipping-providers">Vận chuyển</a>
<a class="text-white" href="${pageContext.request.contextPath}/manager/orders">Đơn</a>
<a class="text-white" href="${pageContext.request.contextPath}/manager/reports">Báo cáo</a>
<a class="text-white" href="${pageContext.request.contextPath}/notifications">Thông báo <span id="notification-count" class="badge bg-danger"></span></a><a class="text-white" href="${pageContext.request.contextPath}/">Trang chủ</a></div></div></nav><main class="container py-4"><c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if><c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
