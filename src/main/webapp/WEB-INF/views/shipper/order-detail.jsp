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

    <title>Chi tiết giao hàng - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="mb-3">

        <a class="btn btn-secondary"
           href="${pageContext.request.contextPath}/shipper/orders">
            Quay lại
        </a>

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

    <div class="card shadow-sm">

        <div class="card-body p-4">

            <h2 class="mb-4">
                Đơn giao hàng #${order.id}
            </h2>

            <table class="table table-bordered">

                <tr>
                    <th width="300">
                        Cửa hàng
                    </th>
                    <td>
                        ${order.shop.name}
                    </td>
                </tr>

                <tr>
                    <th>
                        Người nhận
                    </th>
                    <td>
                        ${order.receiverName}
                    </td>
                </tr>

                <tr>
                    <th>
                        Điện thoại
                    </th>
                    <td>
                        ${order.receiverPhone}
                    </td>
                </tr>

                <tr>
                    <th>
                        Địa chỉ
                    </th>
                    <td>
                        ${order.shippingAddress}
                    </td>
                </tr>

                <tr>
                    <th>
                        Ghi chú
                    </th>
                    <td>
                        ${order.note}
                    </td>
                </tr>

                <tr>
                    <th>
                        Tổng tiền
                    </th>
                    <td>              
                    		<fmt:formatNumber
                    			value="${order.totalAmount}"
                    			type="number"
                    			maxFractionDigits="0"
                    			groupingUsed="true" />
                    		VNĐ                   
                    </td>
                </tr>

                <tr>
                    <th>
                        Trạng thái
                    </th>
                    <td>
                        <strong>
                            ${order.status}
                        </strong>
                    </td>
                </tr>

                <tr>
                    <th>
                        Thời gian phân công
                    </th>
                    <td>
                        ${assignment.assignedAtFormatted}
                    </td>
                </tr>

                <tr>
                    <th>
                        Thời gian lấy hàng
                    </th>
                    <td>
                        ${assignment.pickedUpAtFormatted}
                    </td>
                </tr>

                <tr>
                    <th>
                        Thời gian giao thành công
                    </th>
                    <td>
                        ${assignment.deliveredAtFormatted}
                    </td>
                </tr>

            </table>

            <hr>

            <h5 class="mb-3">
                Cập nhật giao hàng
            </h5>

            <c:if test="${order.status eq 'ASSIGNED'}">

                <form method="post"
                      action="${pageContext.request.contextPath}/shipper/orders/${assignment.id}/status">

                    <input type="hidden"
                           name="${_csrf.parameterName}"
                           value="${_csrf.token}">

                    <input type="hidden"
                           name="status"
                           value="PICKED_UP">

                    <button class="btn btn-primary"
                            type="submit">
                        Đã lấy hàng
                    </button>

                </form>

            </c:if>

            <c:if test="${order.status eq 'PICKED_UP'}">

                <form method="post"
                      action="${pageContext.request.contextPath}/shipper/orders/${assignment.id}/status">

                    <input type="hidden"
                           name="${_csrf.parameterName}"
                           value="${_csrf.token}">

                    <input type="hidden"
                           name="status"
                           value="SHIPPING">

                    <button class="btn btn-warning"
                            type="submit">
                        Bắt đầu giao hàng
                    </button>

                </form>

            </c:if>

            <c:if test="${order.status eq 'SHIPPING'}">

                <div class="d-flex gap-2">

                    <form method="post"
                          action="${pageContext.request.contextPath}/shipper/orders/${assignment.id}/status">

                        <input type="hidden"
                               name="${_csrf.parameterName}"
                               value="${_csrf.token}">

                        <input type="hidden"
                               name="status"
                               value="DELIVERED">

                        <button class="btn btn-success"
                                type="submit">
                            Giao thành công
                        </button>

                    </form>

                    <form method="post"
                          action="${pageContext.request.contextPath}/shipper/orders/${assignment.id}/status">

                        <input type="hidden"
                               name="${_csrf.parameterName}"
                               value="${_csrf.token}">

                        <input type="hidden"
                               name="status"
                               value="DELIVERY_FAILED">

                        <button class="btn btn-danger"
                                type="submit">
                            Giao thất bại
                        </button>

                    </form>

                </div>

            </c:if>

            <c:if test="${order.status eq 'DELIVERED'}">

                <div class="alert alert-success mb-0">
                    Đơn hàng đã giao thành công.
                </div>

            </c:if>

            <c:if test="${order.status eq 'DELIVERY_FAILED'}">

                <div class="alert alert-danger mb-0">
                    Đơn hàng giao thất bại.
                </div>

            </c:if>

        </div>

    </div>

</div>

</body>

</html>