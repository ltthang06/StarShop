<%@ taglib prefix="shop" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jsp" %>

<fmt:setLocale value="vi_VN"/>
<c:url var="topUrl" value="/products/top"/>
<c:url var="fallbackUrl" value="/images/no-image.svg"/>

<main class="container py-5">
    <h1 class="section-title">Top 20 sản phẩm</h1>

    <form action="${topUrl}" method="get" class="row g-2 mb-4">
        <div class="col-md-4">
            <select class="form-select" name="sort">
                <option value="newest" ${sort eq 'newest' ? 'selected' : ''}>
                    Mới nhất
                </option>
                <option value="bestseller" ${sort eq 'bestseller' ? 'selected' : ''}>
                    Bán chạy
                </option>
                <option value="rating" ${sort eq 'rating' ? 'selected' : ''}>
                    Đánh giá cao
                </option>
                <option value="favorites" ${sort eq 'favorites' ? 'selected' : ''}>
                    Được yêu thích nhiều
                </option>
            </select>
        </div>
        <div class="col-md-2">
            <button class="btn btn-danger w-100" type="submit">Xem</button>
        </div>
    </form>

    <c:choose>
        <c:when test="${empty products}">
            <p class="text-muted">Chưa có sản phẩm trong bảng xếp hạng này.</p>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <c:forEach var="product" items="${products}" varStatus="rank">
                    <c:url var="detailUrl" value="/products/${product.id}"/>
                    <div class="col-6 col-md-3">
                        <div class="card product-card h-100 shadow-sm">
                            <img src="${not empty product.imageUrl ? product.imageUrl : fallbackUrl}"
                                 class="card-img-top"
                                 alt="<c:out value='${product.name}'/>">
                            <div class="card-body d-flex flex-column">
                                <span class="badge bg-danger align-self-start mb-2">
                                    #${rankingPage.number * rankingPage.size + rank.count}
                                </span>
                                <small class="text-muted">
                                    <c:out value="${product.categoryName}"/>
                                </small>
                                <h2 class="h6 mt-2">
                                    <c:out value="${product.name}"/>
                                </h2>
                                <p class="small text-muted mb-2">
                                    Đã bán ${product.soldCount} ·
                                    ${product.rating}/5 sao
                                </p>
                                <p class="price mt-auto">
                                    <c:choose>
                                        <c:when test="${not empty product.discountPrice}">
                                            <shop:money value="${product.discountPrice}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <shop:money value="${product.price}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <a class="btn btn-starshop btn-sm" href="${detailUrl}">
                                    Xem chi tiết
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
    <nav class="d-flex justify-content-between align-items-center mt-4" aria-label="Phân trang Top 20">
        <c:choose><c:when test="${rankingPage.hasPrevious()}">
            <c:url var="rankPrev" value="/products/top"><c:param name="sort" value="${sort}"/><c:param name="page" value="${rankingPage.number - 1}"/></c:url>
            <a class="btn btn-outline-danger" href="${rankPrev}">Trang trước</a>
        </c:when><c:otherwise><span></span></c:otherwise></c:choose>
        <span>Trang ${rankingPage.number + 1} / ${rankingPage.totalPages == 0 ? 1 : rankingPage.totalPages}</span>
        <c:choose><c:when test="${rankingPage.hasNext()}">
            <c:url var="rankNext" value="/products/top"><c:param name="sort" value="${sort}"/><c:param name="page" value="${rankingPage.number + 1}"/></c:url>
            <a class="btn btn-outline-danger" href="${rankNext}">Trang sau</a>
        </c:when><c:otherwise><span></span></c:otherwise></c:choose>
    </nav>
</main>

<%@ include file="../common/footer.jsp" %>
