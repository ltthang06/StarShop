<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Khuyến mãi - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="d-flex justify-content-between align-items-center mb-4">

        <div>
            <h2>Quản lý khuyến mãi</h2>
            <div class="text-muted">
                ${shop.name}
            </div>
        </div>

        <a class="btn btn-primary"
           href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/promotions/create">
            + Thêm khuyến mãi
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

    <form method="get"
          class="card card-body mb-4">

        <div class="row g-3">

            <div class="col-md-6">

                <input type="text"
                       name="keyword"
                       value="${keyword}"
                       class="form-control"
                       placeholder="Mã hoặc tên khuyến mãi">

            </div>

            <div class="col-md-3">

                <select name="active"
                        class="form-select">

                    <option value="">
                        Tất cả trạng thái
                    </option>

                    <option value="true"
                        ${selectedActive == true
                            ? 'selected'
                            : ''}>
                        Đang bật
                    </option>

                    <option value="false"
                        ${selectedActive == false
                            ? 'selected'
                            : ''}>
                        Đã tắt
                    </option>

                </select>

            </div>

            <div class="col-md-3">

                <button class="btn btn-primary w-100"
                        type="submit">
                    Tìm kiếm
                </button>

            </div>

        </div>

    </form>

    <div class="card shadow-sm">

        <div class="table-responsive">

            <table class="table table-hover align-middle mb-0">

                <thead class="table-light">

                <tr>
                    <th>Mã</th>
                    <th>Tên</th>
                    <th>Loại</th>
                    <th>Mức giảm</th>
                    <th>Số lượng</th>
                    <th>Thời gian</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>

                </thead>

                <tbody>

                <c:forEach items="${promotions.content}"
                           var="promotion">

                    <tr>

                        <td>
                            ${promotion.code}
                        </td>

                        <td>
                            ${promotion.name}
                        </td>

                        <td>
                            ${promotion.type}
                        </td>

                        <td>

                            <c:choose>

                                <c:when test="${promotion.type == 'PERCENT'}">

                                    <fmt:formatNumber
                                            value="${promotion.discountValue}"
                                            maxFractionDigits="0"/>
                                    %

                                </c:when>

                                <c:when test="${promotion.type == 'FIXED_AMOUNT'}">

                                    <fmt:formatNumber
                                            value="${promotion.discountValue}"
                                            maxFractionDigits="0"
                                            groupingUsed="true"/>
                                    VNĐ

                                </c:when>

                                <c:otherwise>
                                    Miễn phí vận chuyển
                                </c:otherwise>

                            </c:choose>

                        </td>

                        <td>
                            ${promotion.quantity}
                        </td>

                        <td>

                            ${fn:replace(
                                fn:substring(
                                    promotion.startAt,
                                    0,
                                    16
                                ),
                                'T',
                                ' '
                            )}

                            <br>

                            đến

                            <br>

                            ${fn:replace(
                                fn:substring(
                                    promotion.endAt,
                                    0,
                                    16
                                ),
                                'T',
                                ' '
                            )}

                        </td>

                        <td>

                            <c:choose>

                                <c:when test="${promotion.active}">
                                    <span class="badge bg-success">
                                        Đang bật
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="badge bg-secondary">
                                        Đã tắt
                                    </span>
                                </c:otherwise>

                            </c:choose>

                        </td>

                        <td>

                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/promotions/${promotion.id}/edit">
                                Sửa
                            </a>

                            <c:choose>

                                <c:when test="${promotion.active}">

                                    <form method="post"
                                          class="d-inline"
                                          action="${pageContext.request.contextPath}/vendor/shops/${shop.id}/promotions/${promotion.id}/active">

                                        <input type="hidden"
                                               name="${_csrf.parameterName}"
                                               value="${_csrf.token}">

                                        <input type="hidden"
                                               name="active"
                                               value="false">

                                        <button class="btn btn-sm btn-outline-danger"
                                                type="submit">
                                            Tắt
                                        </button>

                                    </form>

                                </c:when>

                                <c:otherwise>

                                    <form method="post"
                                          class="d-inline"
                                          action="${pageContext.request.contextPath}/vendor/shops/${shop.id}/promotions/${promotion.id}/active">

                                        <input type="hidden"
                                               name="${_csrf.parameterName}"
                                               value="${_csrf.token}">

                                        <input type="hidden"
                                               name="active"
                                               value="true">

                                        <button class="btn btn-sm btn-outline-success"
                                                type="submit">
                                            Bật
                                        </button>

                                    </form>

                                </c:otherwise>

                            </c:choose>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty promotions.content}">

                    <tr>

                        <td colspan="8"
                            class="text-center py-4 text-muted">
                            Chưa có khuyến mãi phù hợp.
                        </td>

                    </tr>

                </c:if>

                </tbody>

            </table>

        </div>

    </div>

    <c:if test="${promotions.totalPages > 1}">

        <div class="d-flex justify-content-center mt-4">

            <div class="btn-group">

                <c:forEach begin="0"
                           end="${promotions.totalPages - 1}"
                           var="i">

                    <a class="btn ${i == promotions.number
                        ? 'btn-primary'
                        : 'btn-outline-primary'}"
                       href="?page=${i}&size=${size}&keyword=${keyword}">
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