<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Chi tiết đơn hàng - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">

        <div>

            <h2>
                Đơn hàng #${order.id}
            </h2>

            <div class="text-muted">
                ${shop.name}
            </div>

        </div>

        <span class="badge text-bg-secondary fs-6">
            ${order.status}
        </span>

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

    <div class="row g-4">

        <div class="col-lg-7">

            <div class="card shadow-sm">

                <div class="card-body">

                    <h5 class="mb-3">
                        Sản phẩm
                    </h5>

                    <div class="table-responsive">

                        <table class="table">

                            <thead>

                            <tr>
                                <th>Sản phẩm</th>
                                <th>Số lượng</th>
                                <th>Đơn giá</th>
                                <th>Thành tiền</th>
                            </tr>

                            </thead>

                            <tbody>

                            <c:forEach items="${orderDetails}"
                                       var="detail">

                                <tr>

                                    <td>
                                        ${detail.product.name}
                                    </td>

                                    <td>
                                        ${detail.quantity}
                                    </td>

                                    <td>

                                        <fmt:formatNumber
                                                value="${detail.unitPrice}"
                                                type="number"
                                                groupingUsed="true"
                                                maxFractionDigits="0"/>
                                        VNĐ

                                    </td>

                                    <td>

                                        <fmt:formatNumber
                                                value="${detail.subtotal}"
                                                type="number"
                                                groupingUsed="true"
                                                maxFractionDigits="0"/>
                                        VNĐ

                                    </td>

                                </tr>

                            </c:forEach>

                            </tbody>

                        </table>

                    </div>

                </div>

            </div>

        </div>

        <div class="col-lg-5">

            <div class="card shadow-sm mb-4">

                <div class="card-body">

                    <h5 class="mb-3">
                        Thông tin giao hàng
                    </h5>

                    <p>
                        <strong>Người nhận:</strong>
                        ${order.receiverName}
                    </p>

                    <p>
                        <strong>Số điện thoại:</strong>
                        ${order.receiverPhone}
                    </p>

                    <p>
                        <strong>Địa chỉ:</strong>
                        ${order.shippingAddress}
                    </p>

                    <p class="mb-0">
                        <strong>Ghi chú:</strong>
                        ${empty order.note
                            ? 'Không có'
                            : order.note}
                    </p>

                </div>

            </div>

            <div class="card shadow-sm">

                <div class="card-body">

                    <h5 class="mb-3">
                        Thanh toán
                    </h5>

                    <p>
                        <strong>Tạm tính:</strong>

                        <fmt:formatNumber
                                value="${order.subtotal}"
                                type="number"
                                groupingUsed="true"
                                maxFractionDigits="0"/>
                        VNĐ
                    </p>

                    <p>
                        <strong>Phí giao hàng:</strong>

                        <fmt:formatNumber
                                value="${order.shippingFee}"
                                type="number"
                                groupingUsed="true"
                                maxFractionDigits="0"/>
                        VNĐ
                    </p>

                    <p>
                        <strong>Giảm giá:</strong>

                        <fmt:formatNumber
                                value="${order.discountAmount}"
                                type="number"
                                groupingUsed="true"
                                maxFractionDigits="0"/>
                        VNĐ
                    </p>

                    <p class="fs-5">
                        <strong>Tổng cộng:</strong>

                        <fmt:formatNumber
                                value="${order.totalAmount}"
                                type="number"
                                groupingUsed="true"
                                maxFractionDigits="0"/>
                        VNĐ
                    </p>

                    <p>
                        <strong>Phương thức:</strong>
                        ${order.paymentMethod}
                    </p>

                    <p>
                        <strong>Trạng thái thanh toán:</strong>
                        ${order.paymentStatus}
                    </p>

                </div>

            </div>

        </div>

    </div>

    <div class="mt-4 d-flex gap-2 flex-wrap">

        <c:if test="${order.status == 'NEW'}">

            <form method="post"
                  action="${pageContext.request.contextPath}/vendor/shops/${shop.id}/orders/${order.id}/confirm">

                <input type="hidden"
                       name="${_csrf.parameterName}"
                       value="${_csrf.token}">

                <button type="submit"
                        class="btn btn-success">
                    Xác nhận đơn
                </button>

            </form>

        </c:if>

        <c:if test="${order.status == 'CONFIRMED'}">

            <form method="post"
                  action="${pageContext.request.contextPath}/vendor/shops/${shop.id}/orders/${order.id}/ready">

                <input type="hidden"
                       name="${_csrf.parameterName}"
                       value="${_csrf.token}">

                <button type="submit"
                        class="btn btn-primary">
                    Sẵn sàng giao
                </button>

            </form>

        </c:if>

        <c:if test="${order.status == 'NEW'
                      || order.status == 'CONFIRMED'}">

            <form method="post"
                  action="${pageContext.request.contextPath}/vendor/shops/${shop.id}/orders/${order.id}/cancel"
                  onsubmit="return confirm('Bạn có chắc muốn hủy đơn hàng này?');">

                <input type="hidden"
                       name="${_csrf.parameterName}"
                       value="${_csrf.token}">

                <button type="submit"
                        class="btn btn-outline-danger">
                    Hủy đơn
                </button>

            </form>

        </c:if>

        <a class="btn btn-secondary"
           href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/orders">
            Quay lại danh sách
        </a>

    </div>

</div>

</body>
</html>