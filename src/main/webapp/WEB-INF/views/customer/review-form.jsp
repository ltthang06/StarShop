<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jsp" %>

<c:url var="reviewUrl"
       value="/orders/${target.orderId}/items/${target.detailId}/review"/>
<c:url var="ordersUrl" value="/orders"/>

<main class="container py-5">
    <div class="col-lg-7 mx-auto">
        <a href="${ordersUrl}" class="btn btn-link mb-3">← Đơn hàng của tôi</a>
        <h1 class="section-title">Đánh giá sản phẩm</h1>
        <h2 class="h5 mb-4"><c:out value="${target.productName}"/></h2>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>

        <form action="${reviewUrl}" method="post" enctype="multipart/form-data">
            <input type="hidden" name="${_csrf.parameterName}"
                   value="${_csrf.token}">

            <div class="mb-3">
                <label class="form-label" for="rating">Điểm đánh giá</label>
                <select class="form-select" id="rating" name="rating" required>
                    <option value="5">5 sao</option>
                    <option value="4">4 sao</option>
                    <option value="3">3 sao</option>
                    <option value="2">2 sao</option>
                    <option value="1">1 sao</option>
                </select>
            </div>

            <div class="mb-3">
                <label class="form-label" for="content">Nội dung đánh giá</label>
                <textarea class="form-control" id="content" name="content"
                          rows="6" minlength="50" maxlength="2000" required></textarea>
                <div class="form-text">Tối thiểu 50 ký tự.</div>
            </div>

            <div class="mb-3">
                <label class="form-label" for="image">Ảnh (không bắt buộc)</label>
                <input class="form-control" type="file" id="image" name="image"
                       accept="image/jpeg,image/png,image/webp">
            </div>

            <div class="mb-3">
                <label class="form-label" for="video">Video (không bắt buộc)</label>
                <input class="form-control" type="file" id="video" name="video"
                       accept="video/mp4,video/webm">
            </div>

            <button class="btn btn-danger" type="submit">Gửi đánh giá</button>
        </form>
    </div>
</main>

<%@ include file="../common/footer.jsp" %>
