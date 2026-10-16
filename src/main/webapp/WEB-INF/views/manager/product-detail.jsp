<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="fragments/header.jsp" %>
<a href="${pageContext.request.contextPath}/manager/products" class="text-decoration-none">← Danh sách sản phẩm</a>
<div class="d-flex justify-content-between flex-wrap gap-2 align-items-center my-3"><h1 class="h3 mb-0"><c:out value="${product.name}"/></h1><shop:status value="${product.status}"/></div>
<div class="row g-4"><div class="col-lg-5"><c:choose>
    <c:when test="${empty images}"><img class="img-fluid rounded w-100" src="${pageContext.request.contextPath}/images/no-image.svg" alt="Chưa có ảnh sản phẩm"></c:when>
    <c:otherwise><div class="row g-2"><c:forEach items="${images}" var="image"><div class="col-6"><img class="img-fluid rounded w-100" src="<c:out value='${image.imageUrl}'/>" alt="<c:out value='${product.name}'/>"></div></c:forEach></div></c:otherwise>
</c:choose></div><div class="col-lg-7"><section class="card card-body">
    <dl class="row"><dt class="col-sm-4">Cửa hàng</dt><dd class="col-sm-8"><a href="${pageContext.request.contextPath}/manager/shops/${product.shop.id}"><c:out value="${product.shop.name}"/></a></dd>
    <dt class="col-sm-4">Danh mục</dt><dd class="col-sm-8"><c:out value="${product.category.name}"/></dd>
    <dt class="col-sm-4">Giá bán</dt><dd class="col-sm-8"><shop:money value="${product.price}"/></dd>
    <dt class="col-sm-4">Giá khuyến mãi</dt><dd class="col-sm-8"><shop:money value="${product.discountPrice}"/></dd>
    <dt class="col-sm-4">Tồn kho</dt><dd class="col-sm-8">${product.quantity}</dd>
    <dt class="col-sm-4">Đã bán</dt><dd class="col-sm-8">${product.soldCount}</dd></dl>
    <h2 class="h5">Mô tả</h2><p style="white-space:pre-wrap"><c:out value="${product.description}"/></p>
    <c:if test="${moderation.blocked}"><div class="alert alert-warning">Đang bị quản trị khóa: <c:out value="${moderation.reason}"/></div></c:if>
    <form method="post" action="${pageContext.request.contextPath}/manager/products/${product.id}/moderate">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="blocked" value="${not moderation.blocked}">
        <c:if test="${not moderation.blocked}"><label for="moderation-reason" class="form-label">Lý do khóa (10–500 ký tự)</label><textarea id="moderation-reason" name="reason" class="form-control mb-3" minlength="10" maxlength="500" required></textarea></c:if>
        <button class="btn ${moderation.blocked ? 'btn-outline-success' : 'btn-outline-danger'}">${moderation.blocked ? 'Mở kiểm duyệt' : 'Khóa sản phẩm'}</button>
    </form>
</section></div></div>
<%@ include file="fragments/footer.jsp" %>
