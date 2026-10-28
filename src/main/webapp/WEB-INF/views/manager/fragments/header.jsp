<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="shop" tagdir="/WEB-INF/tags" %>
<c:set var="managementAdmin" value="${pageContext.request.isUserInRole('ADMIN')}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>StarShop · Quản trị</title>
    <shop:theme/>
    <link href="${pageContext.request.contextPath}/css/manager.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-dark manager-navbar py-3" aria-label="Điều hướng quản trị">
    <div class="container gap-2">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/manager"><span class="manager-brand-mark" aria-hidden="true">S</span>${managementAdmin ? 'StarShop Admin' : 'StarShop Manager'}</a>
        <button class="btn btn-outline-light manager-menu-toggle" id="manager-menu-toggle" type="button"
                aria-expanded="false" aria-controls="manager-menu">Menu</button>
        <div class="manager-menu" id="manager-menu">
            <c:if test="${managementAdmin}"><a class="nav-link" href="${pageContext.request.contextPath}/manager/users">Người dùng</a></c:if>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/shops">Cửa hàng</a>
            <c:if test="${managementAdmin}"><a class="nav-link" href="${pageContext.request.contextPath}/manager/categories">Danh mục</a></c:if>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/products">Sản phẩm</a>
            <c:if test="${managementAdmin}">
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/promotions">Voucher hệ thống</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/shipping-providers">Vận chuyển</a>
            </c:if>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/orders">Đơn hàng</a>
            <c:if test="${pageContext.request.isUserInRole('ADMIN')}"><a class="nav-link" href="${pageContext.request.contextPath}/manager/payments">Đối soát thanh toán</a></c:if>
            <a class="nav-link" href="${pageContext.request.contextPath}/manager/reports">Báo cáo</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/notifications">Thông báo <span id="notification-count" class="badge bg-danger"></span></a>
            <a class="nav-link" href="${pageContext.request.contextPath}/">Trang chủ</a>
        </div>
    </div>
</nav>
<main class="container py-4 manager-content">
    <c:if test="${not empty success}"><div class="alert alert-success" role="status"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
