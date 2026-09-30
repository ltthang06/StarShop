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

    <title>Khuyến mãi - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="row justify-content-center">

        <div class="col-lg-8">

            <div class="card shadow-sm">

                <div class="card-body p-4">

                    <h2>
                        ${editing
                            ? 'Chỉnh sửa khuyến mãi'
                            : 'Thêm khuyến mãi'}
                    </h2>

                    <div class="text-muted mb-4">
                        ${shop.name}
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger">
                            ${errorMessage}
                        </div>
                    </c:if>

                    <form id="promotionForm"
                          method="post"
                          action="${pageContext.request.contextPath}/vendor/shops/${shop.id}/promotions${editing ? '/' : '/create'}${editing ? promotionId : ''}${editing ? '/edit' : ''}">

                        <input type="hidden"
                               name="${_csrf.parameterName}"
                               value="${_csrf.token}">

                        <div class="row">

                            <div class="col-md-5 mb-3">

                                <label class="form-label">
                                    Mã khuyến mãi
                                </label>

                                <input type="text"
                                       name="code"
                                       value="${promotionRequest.code}"
                                       class="form-control"
                                       maxlength="50"
                                       placeholder="VD: FLOWER10"
                                       required>

                            </div>

                            <div class="col-md-7 mb-3">

                                <label class="form-label">
                                    Tên khuyến mãi
                                </label>

                                <input type="text"
                                       name="name"
                                       value="${promotionRequest.name}"
                                       class="form-control"
                                       maxlength="150"
                                       required>

                            </div>

                        </div>

                        <div class="mb-3">

                            <label class="form-label">
                                Mô tả
                            </label>

                            <textarea name="description"
                                      class="form-control"
                                      rows="3"
                                      maxlength="500">${promotionRequest.description}</textarea>

                        </div>

                        <div class="row">

                            <div class="col-md-6 mb-3">

                                <label class="form-label">
                                    Loại khuyến mãi
                                </label>

                                <select name="type"
                                        class="form-select"
                                        required>

                                    <option value="">
                                        Chọn loại
                                    </option>

                                    <c:forEach items="${types}"
                                               var="type">

                                        <option value="${type}"
                                            ${promotionRequest.type == type
                                                ? 'selected'
                                                : ''}>
                                            ${type}
                                        </option>

                                    </c:forEach>

                                </select>

                            </div>

                            <div class="col-md-6 mb-3">

                                <label class="form-label">
                                    Giá trị giảm
                                </label>

                                <input type="number"
                                       name="discountValue"
                                       value="${promotionRequest.discountValueInput}"
                                       class="form-control"
                                       min="0"
                                       step="0.01">

                            </div>

                        </div>

                        <div class="row">

                            <div class="col-md-4 mb-3">

                                <label class="form-label">
                                    Đơn tối thiểu (VNĐ)
                                </label>

                                <input type="number"
                                       name="minOrderAmount"
                                       value="${promotionRequest.minOrderAmountInput}"
                                       class="form-control"
                                       min="0"
                                       step="1000">

                            </div>

                            <div class="col-md-4 mb-3">

                                <label class="form-label">
                                    Giảm tối đa (VNĐ)
                                </label>

                                <input type="number"
                                       name="maxDiscountAmount"
                                       value="${promotionRequest.maxDiscountAmountInput}"
                                       class="form-control"
                                       min="0"
                                       step="1000">

                            </div>

                            <div class="col-md-4 mb-3">

                                <label class="form-label">
                                    Số lượng mã
                                </label>

                                <input type="number"
                                       name="quantity"
                                       value="${promotionRequest.quantity}"
                                       class="form-control"
                                       min="0"
                                       required>

                            </div>

                        </div>

                        <div class="row">

                            <div class="col-md-6 mb-3">

                                <label class="form-label">
                                    Bắt đầu
                                </label>

                                <div class="row g-2">

                                    <div class="col-6">

                                        <label class="form-label small text-muted">
                                            Ngày
                                        </label>

                                        <input type="date"
                                               id="startDate"
                                               class="form-control"
                                               required>

                                    </div>

                                    <div class="col-3">

                                        <label class="form-label small text-muted">
                                            Giờ
                                        </label>

                                        <select id="startHour"
                                                class="form-select"
                                                required>
                                        </select>

                                    </div>

                                    <div class="col-3">

                                        <label class="form-label small text-muted">
                                            Phút
                                        </label>

                                        <select id="startMinute"
                                                class="form-select"
                                                required>
                                        </select>

                                    </div>

                                </div>

                                <input type="hidden"
                                       name="startAt"
                                       id="startAt"
                                       value="${promotionRequest.startAt}">

                            </div>

                            <div class="col-md-6 mb-3">

                                <label class="form-label">
                                    Kết thúc
                                </label>

                                <div class="row g-2">

                                    <div class="col-6">

                                        <label class="form-label small text-muted">
                                            Ngày
                                        </label>

                                        <input type="date"
                                               id="endDate"
                                               class="form-control"
                                               required>

                                    </div>

                                    <div class="col-3">

                                        <label class="form-label small text-muted">
                                            Giờ
                                        </label>

                                        <select id="endHour"
                                                class="form-select"
                                                required>
                                        </select>

                                    </div>

                                    <div class="col-3">

                                        <label class="form-label small text-muted">
                                            Phút
                                        </label>

                                        <select id="endMinute"
                                                class="form-select"
                                                required>
                                        </select>

                                    </div>

                                </div>

                                <input type="hidden"
                                       name="endAt"
                                       id="endAt"
                                       value="${promotionRequest.endAt}">

                            </div>

                        </div>

                        <div class="form-check mb-4">

                            <input class="form-check-input"
                                   type="checkbox"
                                   name="active"
                                   value="true"
                                   id="active"
                                ${promotionRequest.active
                                    ? 'checked'
                                    : ''}>

                            <label class="form-check-label"
                                   for="active">
                                Kích hoạt khuyến mãi
                            </label>

                        </div>

                        <button type="submit"
                                class="btn btn-primary">
                            Lưu khuyến mãi
                        </button>

                        <a class="btn btn-secondary"
                           href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/promotions">
                            Hủy
                        </a>

                    </form>

                </div>

            </div>

        </div>

    </div>

</div>

<script>

    function pad(value) {
        return String(value).padStart(2, '0');
    }

    function createTimeOptions(
            hourSelectId,
            minuteSelectId) {

        const hourSelect =
            document.getElementById(hourSelectId);

        const minuteSelect =
            document.getElementById(minuteSelectId);

        hourSelect.innerHTML = '';
        minuteSelect.innerHTML = '';

        for (let hour = 0; hour <= 23; hour++) {

            const option =
                document.createElement('option');

            option.value = pad(hour);
            option.textContent = pad(hour);

            hourSelect.appendChild(option);
        }

        for (let minute = 0; minute <= 59; minute++) {

            const option =
                document.createElement('option');

            option.value = pad(minute);
            option.textContent = pad(minute);

            minuteSelect.appendChild(option);
        }
    }

    function loadDateTime(
            hiddenId,
            dateId,
            hourId,
            minuteId) {

        const value =
            document.getElementById(hiddenId).value;

        if (!value) {
            return;
        }

        const parts =
            value.split('T');

        if (parts.length !== 2) {
            return;
        }

        const timeParts =
            parts[1].split(':');

        if (timeParts.length < 2) {
            return;
        }

        document.getElementById(dateId).value =
            parts[0];

        document.getElementById(hourId).value =
            timeParts[0];

        document.getElementById(minuteId).value =
            timeParts[1];
    }

    function buildDateTime(
            hiddenId,
            dateId,
            hourId,
            minuteId) {

        const date =
            document.getElementById(dateId).value;

        const hour =
            document.getElementById(hourId).value;

        const minute =
            document.getElementById(minuteId).value;

        if (!date
                || hour === ''
                || minute === '') {

            return false;
        }

        document.getElementById(hiddenId).value =
            date
            + 'T'
            + hour
            + ':'
            + minute;

        return true;
    }

    createTimeOptions(
        'startHour',
        'startMinute'
    );

    createTimeOptions(
        'endHour',
        'endMinute'
    );

    loadDateTime(
        'startAt',
        'startDate',
        'startHour',
        'startMinute'
    );

    loadDateTime(
        'endAt',
        'endDate',
        'endHour',
        'endMinute'
    );

    document.getElementById(
        'promotionForm'
    ).addEventListener(
        'submit',
        function(event) {

            const startValid =
                buildDateTime(
                    'startAt',
                    'startDate',
                    'startHour',
                    'startMinute'
                );

            const endValid =
                buildDateTime(
                    'endAt',
                    'endDate',
                    'endHour',
                    'endMinute'
                );

            if (!startValid
                    || !endValid) {

                event.preventDefault();

                alert(
                    'Vui lòng chọn đầy đủ ngày, giờ và phút.'
                );

                return;
            }

            const startAt =
                document.getElementById(
                    'startAt'
                ).value;

            const endAt =
                document.getElementById(
                    'endAt'
                ).value;

            if (startAt >= endAt) {

                event.preventDefault();

                alert(
                    'Thời gian kết thúc phải sau thời gian bắt đầu.'
                );
            }
        }
    );

</script>

</body>

</html>