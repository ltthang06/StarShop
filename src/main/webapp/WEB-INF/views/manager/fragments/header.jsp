<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="shop" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>StarShop · Quản trị</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/manager.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-dark manager-navbar py-3" aria-label="Điều hướng quản trị">
    <div class="container gap-2">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/manager"><span class="manager-brand-mark" aria-hidden="true">S</span>StarShop Admin</a>
        <button class="btn btn-outline-light manager-menu-toggle" id="manager-menu-toggle" type="button"
                aria-expanded="false" aria-controls="manager-menu">Menu</button>
        <div class="manager-menu" id="manager-menu">
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/users">Người dùng</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/shops">Cửa hàng</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/categories">Danh mục</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/products">Sản phẩm</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/promotions">Voucher</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/shipping-providers">Vận chuyển</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/orders">Đơn hàng</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/reports">Báo cáo</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/notifications">Thông báo <span id="notification-count" class="badge bg-danger"></span></a>
            <a class="nav-link" href="${pageContext.request.contextPath}/">Trang chủ</a>
        </div>
    </div>
</nav>
<main class="container py-4 manager-content">
    <c:if test="${not empty success}"><div class="alert alert-success" role="status"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
