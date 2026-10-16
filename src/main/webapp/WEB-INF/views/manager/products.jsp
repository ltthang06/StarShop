<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="fragments/header.jsp" %>
<h1 class="h3">Kiểm duyệt sản phẩm</h1>
<p class="text-muted">Xem ảnh và nội dung trước khi xử lý. Mở kiểm duyệt vẫn giữ sản phẩm ẩn để người bán kiểm tra và bật lại.</p>
<form class="row g-2 align-items-end mb-4">
    <div class="col-md-4"><label for="product-keyword" class="form-label">Tên sản phẩm</label><input id="product-keyword" class="form-control" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Nhập tên sản phẩm"></div>
    <div class="col-md-3"><label for="product-shop" class="form-label">Cửa hàng</label><select id="product-shop" class="form-select" name="shopId"><option value="">Tất cả cửa hàng</option><c:forEach items="${shopOptions}" var="s"><option value="${s.id}" ${shopId == s.id ? 'selected' : ''}><c:out value="${s.name}"/></option></c:forEach></select></div>
    <div class="col-md-3"><label for="product-status" class="form-label">Trạng thái</label><select id="product-status" class="form-select" name="status"><option value="">Mọi trạng thái</option><c:forEach items="${statuses}" var="s"><option value="${s}" ${selectedStatus == s ? 'selected' : ''}>${viewFormat.label(s)}</option></c:forEach></select></div>
    <div class="col-md-2"><button class="btn btn-primary">Tìm kiếm</button></div>
</form>
<c:url var="fallbackImage" value="/images/no-image.svg"/>
<c:forEach items="${products.content}" var="p">
    <div class="card card-body mb-3 shadow-sm">
        <div class="d-flex gap-3 align-items-start mb-3">
            <img class="manager-product-image" src="<c:out value='${empty thumbnails[p.id] ? fallbackImage : thumbnails[p.id]}'/>" alt="<c:out value='${p.name}'/>">
            <div class="flex-grow-1"><h2 class="h5"><a class="text-decoration-none" href="${pageContext.request.contextPath}/manager/products/${p.id}"><c:out value="${p.name}"/></a></h2>
                <p class="small text-muted mb-2"><c:out value="${p.shop.name}"/> · <c:out value="${p.category.name}"/></p>
                <p class="mb-2"><strong><shop:money value="${p.price}"/></strong> · Tồn: ${p.quantity}</p><shop:status value="${p.status}"/>
            </div>
        </div>
        <c:set var="m" value="${moderations[p.id]}"/>
        <c:if test="${m.blocked}"><div class="alert alert-warning">Lý do khóa: <c:out value="${m.reason}"/> · <c:out value="${m.moderatorEmail}"/></div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/manager/products/${p.id}/moderate" class="d-flex gap-2 flex-wrap">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="blocked" value="${not m.blocked}">
            <c:if test="${not m.blocked}"><label class="form-label mb-0 w-100" for="reason-${p.id}">Lý do khóa</label><input class="form-control" id="reason-${p.id}" name="reason" minlength="10" maxlength="500" required placeholder="Nêu lý do từ 10 đến 500 ký tự"></c:if>
            <button class="btn ${m.blocked ? 'btn-outline-success' : 'btn-outline-danger'}">${m.blocked ? 'Mở kiểm duyệt' : 'Khóa sản phẩm'}</button>
            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/manager/products/${p.id}">Xem chi tiết</a>
        </form>
    </div>
</c:forEach>
<c:if test="${empty products.content}"><div class="alert alert-secondary">Không có sản phẩm phù hợp.</div></c:if>
<c:set var="paged" value="${products}"/><%@ include file="fragments/paging.jsp" %>
<%@ include file="fragments/footer.jsp" %>
