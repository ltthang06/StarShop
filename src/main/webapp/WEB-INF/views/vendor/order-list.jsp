<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Đơn hàng - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="mb-4">

        <h2>Quản lý đơn hàng</h2>

        <div class="text-muted">
            ${shop.name}
        </div>

    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            ${successMessage}
        </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ${errorMessage}
        </div>
    </c:if>

    <form method="get"
          class="card card-body mb-4">

        <div class="row g-3">

            <div class="col-md-5">

                <input type="text"
                       name="keyword"
                       value="${keyword}"
                       class="form-control"
                       placeholder="Tên hoặc số điện thoại người nhận">

            </div>

            <div class="col-md-3">

                <select name="status"
                        class="form-select">

                    <option value="">
                        Tất cả trạng thái
                    </option>

                    <c:forEach items="${statuses}"
                               var="status">

                        <option value="${status}"
                            ${selectedStatus == status
                                ? 'selected'
                                : ''}>
                            ${status}
                        </option>

                    </c:forEach>

                </select>

            </div>

            <div class="col-md-2">

                <select name="size"
                        class="form-select">

                    <option value="5"
                        ${size == 5 ? 'selected' : ''}>
                        5
                    </option>

                    <option value="10"
                        ${size == 10 ? 'selected' : ''}>
                        10
                    </option>

                    <option value="20"
                        ${size == 20 ? 'selected' : ''}>
                        20
                    </option>

                </select>

            </div>

            <div class="col-md-2">

                <button type="submit"
                        class="btn btn-primary w-100">
                    Tìm kiếm
                </button>

            </div>

        </div>

    </form>

    <div class="card shadow-sm">

        <div class="table-responsive">

            <table class="table table-hover mb-0">

                <thead class="table-light">

                <tr>
                    <th>ID</th>
                    <th>Người nhận</th>
                    <th>SĐT</th>
                    <th>Tổng tiền</th>
                    <th>Thanh toán</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>

                </thead>

                <tbody>

                <c:forEach items="${orders.content}"
                           var="order">

                    <tr>

                        <td>
                            #${order.id}
                        </td>

                        <td>
                            ${order.receiverName}
                        </td>

                        <td>
                            ${order.receiverPhone}
                        </td>

                        <td>

                            <fmt:formatNumber
                                    value="${order.totalAmount}"
                                    type="number"
                                    groupingUsed="true"
                                    maxFractionDigits="0"/>
                            VNĐ

                        </td>

                        <td>
                            ${order.paymentMethod}
                            /
                            ${order.paymentStatus}
                        </td>

                        <td>

                            <span class="badge text-bg-secondary">
                                ${order.status}
                            </span>

                        </td>

                        <td>

                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/orders/${order.id}">
                                Chi tiết
                            </a>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty orders.content}">

                    <tr>

                        <td colspan="7"
                            class="text-center py-4 text-muted">
                            Chưa có đơn hàng phù hợp.
                        </td>

                    </tr>

                </c:if>

                </tbody>

            </table>

        </div>

    </div>

    <c:if test="${orders.totalPages > 0}">

        <div class="d-flex justify-content-center mt-4">

            <div class="btn-group">

                <c:forEach begin="0"
                           end="${orders.totalPages - 1}"
                           var="i">

                    <c:url var="pageUrl"
                           value="/vendor/shops/${shop.id}/orders">

                        <c:param name="page"
                                 value="${i}"/>

                        <c:param name="size"
                                 value="${size}"/>

                        <c:param name="keyword"
                                 value="${keyword}"/>

                        <c:if test="${not empty selectedStatus}">
                            <c:param name="status"
                                     value="${selectedStatus}"/>
                        </c:if>

                    </c:url>

                    <a href="${pageUrl}"
                       class="btn ${i == orders.number
                           ? 'btn-primary'
                           : 'btn-outline-primary'}">
                        ${i + 1}
                    </a>

                </c:forEach>

            </div>

        </div>

    </c:if>

    <div class="mt-4">

        <a class="btn btn-secondary"
           href="${pageContext.request.contextPath}/vendor/dashboard">
            Quay lại Dashboard
        </a>

    </div>

</div>

</body>
</html>