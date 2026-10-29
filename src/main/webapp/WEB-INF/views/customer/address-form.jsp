<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jsp" %>

<c:url var="profileUrl" value="/profile"/>
<c:url var="submitUrl" value="${formAction}"/>
<c:url var="addressPickerScript" value="/js/address-picker.js"/>
<c:url var="addressDataUrl" value="/data/vietnam-addresses.json"/>

<main class="container py-5">
    <div class="col-lg-7 mx-auto">
        <h1 class="section-title">
            ${empty address.id ? 'Thêm địa chỉ' : 'Sửa địa chỉ'}
        </h1>

        <form action="${submitUrl}" method="post" class="card shadow-sm"
              data-address-picker data-province="<c:out value='${address.province}'/>"
              data-ward="<c:out value='${address.ward}'/>"
              data-source="${addressDataUrl}">
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
                    <div class="col-md-6">
                        <label class="form-label" for="province">Tỉnh/Thành phố</label>
                        <select class="form-select" id="province" name="province" required disabled>
                            <option value="">Đang tải danh sách...</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="ward">Phường/Xã</label>
                        <select class="form-select" id="ward" name="ward" required disabled>
                            <option value="">Chọn tỉnh/thành phố trước</option>
                        </select>
                    </div>
                    <div class="col-12">
                        <div class="form-text">Địa chỉ hiện dùng cấp Tỉnh/Thành phố và Phường/Xã. Ghi số nhà, tên đường ở ô phía trên.</div>
                        <div class="small text-danger mt-1" id="addressPickerStatus" role="status" aria-live="polite"></div>
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

<script defer src="${addressPickerScript}"></script>
<%@ include file="../common/footer.jsp" %>
