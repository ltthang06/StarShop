<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Đơn giao hàng - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">

        <div>

            <h2 class="mb-1">
                Đơn giao hàng
            </h2>

            <div class="text-muted">
                Shipper: ${shipper.fullName}
            </div>

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

    <div class="row mb-4">

        <div class="col-md-3 mb-3">

            <div class="card shadow-sm">
                <div class="card-body">

                    <div class="text-muted">
                        Tổng đơn
                    </div>

                    <h3>
                        ${totalAssignments}
                    </h3>

                </div>
            </div>

        </div>

        <div class="col-md-3 mb-3">

            <div class="card shadow-sm">
                <div class="card-body">

                    <div class="text-muted">
                        Đã phân công
                    </div>

                    <h3>
                        ${assignedCount}
                    </h3>

                </div>
            </div>

        </div>

        <div class="col-md-3 mb-3">

            <div class="card shadow-sm">
                <div class="card-body">

                    <div class="text-muted">
                        Đang giao
                    </div>

                    <h3>
                        ${shippingCount}
                    </h3>

                </div>
            </div>

        </div>

        <div class="col-md-3 mb-3">

            <div class="card shadow-sm">
                <div class="card-body">

                    <div class="text-muted">
                        Đã giao
                    </div>

                    <h3>
                        ${deliveredCount}
                    </h3>

                </div>
            </div>

        </div>

    </div>

    <div class="card shadow-sm">

        <div class="card-body">

            <div class="table-responsive">

                <table class="table table-bordered align-middle">

                    <thead>

                    <tr>

                        <th>
                            Mã đơn
                        </th>

                        <th>
                            Cửa hàng
                        </th>

                        <th>
                            Người nhận
                        </th>

                        <th>
                            Điện thoại
                        </th>

                        <th>
                            Trạng thái
                        </th>

                        <th>
                            Phân công lúc
                        </th>

                        <th>
                            Thao tác
                        </th>

                    </tr>

                    </thead>

                    <tbody>

                    <c:forEach items="${assignments.content}"
                               var="assignment">

                        <tr>

                            <td>
                                #${assignment.order.id}
                            </td>

                            <td>
                                ${assignment.order.shop.name}
                            </td>

                            <td>
                                ${assignment.order.receiverName}
                            </td>

                            <td>
                                ${assignment.order.receiverPhone}
                            </td>

                            <td>

                                <span class="badge bg-secondary">
                                    ${assignment.order.status}
                                </span>

                            </td>

                            <td>
                                ${assignment.assignedAtFormatted}
                            </td>

                            <td>

                                <a class="btn btn-sm btn-primary"
                                   href="${pageContext.request.contextPath}/shipper/orders/${assignment.id}">

                                    Chi tiết

                                </a>

                            </td>

                        </tr>

                    </c:forEach>

                    <c:if test="${empty assignments.content}">

                        <tr>

                            <td colspan="7"
                                class="text-center text-muted">

                                Chưa có đơn hàng được phân công.

                            </td>

                        </tr>

                    </c:if>

                    </tbody>

                </table>

            </div>

            <c:if test="${assignments.totalPages > 1}">

                <nav>

                    <ul class="pagination">

                        <c:forEach begin="0"
                                   end="${assignments.totalPages - 1}"
                                   var="i">

                            <li class="page-item ${i == assignments.number ? 'active' : ''}">

                                <a class="page-link"
                                   href="${pageContext.request.contextPath}/shipper/orders?page=${i}&size=${assignments.size}">

                                    ${i + 1}

                                </a>

                            </li>

                        </c:forEach>

                    </ul>

                </nav>

            </c:if>

        </div>

    </div>

</div>

</body>

</html>