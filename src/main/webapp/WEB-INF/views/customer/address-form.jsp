<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jsp" %>

<c:url var="profileUrl" value="/profile"/>
<c:url var="submitUrl" value="${formAction}"/>

<main class="container py-5">
    <div class="col-lg-7 mx-auto">
        <h1 class="section-title">
            ${empty address.id ? 'Thêm địa chỉ' : 'Sửa địa chỉ'}
        </h1>

        <form action="${submitUrl}" method="post" class="card shadow-sm">
            <div class="card-body">
                <input type="hidden" name="${_csrf.parameterName}"
                       value="${_csrf.token}">
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label" for="recipientName">
                            Người nhận
                        </label>
                        <input class="form-control" id="recipientName"
                               name="recipientName"
                               value="<c:out value='${address.recipientName}'/>"
                               maxlength="100" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="phone">Số điện thoại</label>
                        <input class="form-control" id="phone" name="phone"
                               value="<c:out value='${address.phone}'/>"
                               maxlength="20" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="addressLine">
                            Số nhà, tên đường
                        </label>
                        <input class="form-control" id="addressLine"
                               name="addressLine"
                               value="<c:out value='${address.addressLine}'/>"
                               maxlength="255" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="ward">Phường, xã</label>
                        <input class="form-control" id="ward" name="ward"
                               value="<c:out value='${address.ward}'/>"
                               maxlength="100">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="district">Quận, huyện</label>
                        <input class="form-control" id="district" name="district"
                               value="<c:out value='${address.district}'/>"
                               maxlength="100">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="province">Tỉnh, thành phố</label>
                        <input class="form-control" id="province" name="province"
                               value="<c:out value='${address.province}'/>"
                               maxlength="100">
                    </div>
                    <div class="col-12">
                        <label class="form-check-label">
                            <input class="form-check-input" type="checkbox"
                                   name="makeDefault" value="true"
                                   ${address.defaultAddress ? 'checked' : ''}>
                            Đặt làm địa chỉ mặc định
                        </label>
                    </div>
                </div>

                <div class="d-flex gap-2 mt-4">
                    <button class="btn btn-danger" type="submit">Lưu địa chỉ</button>
                    <a href="${profileUrl}" class="btn btn-outline-secondary">
                        Quay lại
                    </a>
                </div>
            </div>
        </form>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
