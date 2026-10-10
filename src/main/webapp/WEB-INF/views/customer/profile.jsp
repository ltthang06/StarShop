<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jsp" %>

<c:url var="profileUrl" value="/profile"/>
<c:url var="newAddressUrl" value="/profile/addresses/new"/>

<main class="container py-5">
    <h1 class="section-title">Hồ sơ cá nhân</h1>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}"/></div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-5">
            <div class="card shadow-sm">
                <div class="card-body">
                    <h2 class="h5 mb-3">Thông tin tài khoản</h2>
                    <form action="${profileUrl}" method="post">
                        <input type="hidden" name="${_csrf.parameterName}"
                               value="${_csrf.token}">
                        <div class="mb-3">
                            <label class="form-label" for="fullName">Họ tên</label>
                            <input class="form-control" id="fullName" name="fullName"
                                   value="<c:out value='${user.fullName}'/>"
                                   maxlength="100" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Email</label>
                            <input class="form-control"
                                   value="<c:out value='${user.email}'/>"
                                   disabled>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="phone">Số điện thoại</label>
                            <input class="form-control" id="phone" name="phone"
                                   value="<c:out value='${user.phone}'/>"
                                   maxlength="20">
                        </div>
                        <button class="btn btn-danger" type="submit">
                            Lưu thông tin
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="h5 mb-0">Địa chỉ nhận hàng</h2>
                <a href="${newAddressUrl}" class="btn btn-outline-danger btn-sm">
                    Thêm địa chỉ
                </a>
            </div>

            <c:choose>
                <c:when test="${empty addresses}">
                    <p class="text-muted">Anh chưa lưu địa chỉ nào.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="address" items="${addresses}">
                        <c:url var="editUrl"
                               value="/profile/addresses/${address.id}/edit"/>
                        <c:url var="defaultUrl"
                               value="/profile/addresses/${address.id}/default"/>
                        <c:url var="deleteUrl"
                               value="/profile/addresses/${address.id}/delete"/>

                        <div class="card shadow-sm mb-3">
                            <div class="card-body">
                                <div class="d-flex justify-content-between">
                                    <strong><c:out value="${address.recipientName}"/></strong>
                                    <c:if test="${address.defaultAddress}">
                                        <span class="badge text-bg-success">Mặc định</span>
                                    </c:if>
                                </div>
                                <p class="mb-1">
                                    <c:out value="${address.phone}"/>
                                </p>
                                <p class="text-muted">
                                    <c:out value="${address.addressLine}"/>,
                                    <c:out value="${address.ward}"/>,
                                    <c:out value="${address.district}"/>,
                                    <c:out value="${address.province}"/>
                                </p>
                                <div class="d-flex gap-2 flex-wrap">
                                    <a href="${editUrl}"
                                       class="btn btn-outline-secondary btn-sm">Sửa</a>
                                    <c:if test="${not address.defaultAddress}">
                                        <form action="${defaultUrl}" method="post">
                                            <input type="hidden"
                                                   name="${_csrf.parameterName}"
                                                   value="${_csrf.token}">
                                            <button class="btn btn-outline-success btn-sm"
                                                    type="submit">
                                                Đặt mặc định
                                            </button>
                                        </form>
                                    </c:if>
                                    <form action="${deleteUrl}" method="post">
                                        <input type="hidden"
                                               name="${_csrf.parameterName}"
                                               value="${_csrf.token}">
                                        <button class="btn btn-outline-danger btn-sm"
                                                type="submit">Xóa</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
