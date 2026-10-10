<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="homeUrl" value="/"/>
<c:url var="productsUrl" value="/products"/>
<c:url var="cartUrl" value="/cart"/>
<c:url var="wishlistUrl" value="/wishlist"/>
<c:url var="profileUrl" value="/profile"/>
<c:url var="ordersUrl" value="/orders"/>
<c:url var="loginUrl" value="/login"/>
<c:url var="registerUrl" value="/register"/>
<c:url var="logoutUrl" value="/logout"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StarShop - Cửa Hàng Hoa</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css"
          rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-danger">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${homeUrl}">
            <i class="bi bi-flower1"></i> StarShop
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                data-bs-target="#navbarMain" aria-label="Mở menu">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarMain">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" href="${homeUrl}">Trang chủ</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${productsUrl}">Sản phẩm</a>
                </li>
            </ul>

            <form class="d-flex me-3" action="${productsUrl}" method="get">
                <input class="form-control me-2" type="search" name="keyword"
                       placeholder="Tìm kiếm hoa..." aria-label="Tìm sản phẩm">
                <button class="btn btn-outline-light" type="submit"
                        aria-label="Tìm kiếm">
                    <i class="bi bi-search"></i>
                </button>
            </form>

            <ul class="navbar-nav align-items-lg-center">
                <li class="nav-item">
                    <a class="nav-link" href="${cartUrl}">
                        <i class="bi bi-cart3"></i> Giỏ hàng
                    </a>
                </li>
                <c:choose>
                    <c:when test="${not empty pageContext.request.userPrincipal}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#"
                               data-bs-toggle="dropdown">
                                <i class="bi bi-person-circle"></i>
                                <c:out value="${pageContext.request.userPrincipal.name}"/>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${profileUrl}">Hồ sơ</a></li>
                                <li><a class="dropdown-item" href="${wishlistUrl}">Yêu thích</a></li>
                                <li><a class="dropdown-item" href="${ordersUrl}">Đơn hàng</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li class="px-3">
                                    <form action="${logoutUrl}" method="post">
                                        <input type="hidden" name="${_csrf.parameterName}"
                                               value="${_csrf.token}">
                                        <button class="dropdown-item p-0" type="submit">
                                            Đăng xuất
                                        </button>
                                    </form>
                                </li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link" href="${loginUrl}">Đăng nhập</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${registerUrl}">Đăng ký</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>
