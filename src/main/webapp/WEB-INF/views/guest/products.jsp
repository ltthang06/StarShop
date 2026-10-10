<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jsp" %>

<fmt:setLocale value="vi_VN"/>
<c:url var="productsUrl" value="/products"/>

<main class="container py-5">
    <h1 class="section-title">Sản phẩm</h1>

    <form action="${productsUrl}" method="get" class="row g-3 mb-4">
        <div class="col-md-5">
            <input class="form-control" name="keyword"
                   value="<c:out value='${keyword}'/>"
                   placeholder="Tìm tên sản phẩm">
        </div>
        <div class="col-md-3">
            <select class="form-select" name="categoryId">
                <option value="">Tất cả danh mục</option>
                <c:forEach var="category" items="${categories}">
                    <option value="${category.id}"
                        ${category.id eq categoryId ? 'selected' : ''}>
                        <c:out value="${category.name}"/>
                    </option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-3">
            <select class="form-select" name="sort">
                <option value="newest" ${sort eq 'newest' ? 'selected' : ''}>Mới nhất</option>
                <option value="bestseller" ${sort eq 'bestseller' ? 'selected' : ''}>Bán chạy</option>
                <option value="rating" ${sort eq 'rating' ? 'selected' : ''}>Đánh giá cao</option>
                <option value="price-asc" ${sort eq 'price-asc' ? 'selected' : ''}>Giá tăng dần</option>
                <option value="price-desc" ${sort eq 'price-desc' ? 'selected' : ''}>Giá giảm dần</option>
            </select>
        </div>
        <div class="col-md-1">
            <button class="btn btn-danger w-100" type="submit">Lọc</button>
        </div>
    </form>

    <c:choose>
        <c:when test="${products.empty}">
            <p class="text-muted">Không tìm thấy sản phẩm phù hợp.</p>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <c:forEach var="product" items="${products.content}">
                    <c:url var="detailUrl" value="/products/${product.id}"/>
                    <c:url var="fallbackUrl" value="/images/no-image.svg"/>
                    <div class="col-6 col-md-3">
                        <div class="card product-card h-100 shadow-sm">
                            <img src="${not empty product.imageUrl ? product.imageUrl : fallbackUrl}"
                                 class="card-img-top"
                                 alt="<c:out value='${product.name}'/>">
                            <div class="card-body d-flex flex-column">
                                <small class="text-muted">
                                    <c:out value="${product.categoryName}"/>
                                </small>
                                <h2 class="h6 mt-2">
                                    <c:out value="${product.name}"/>
                                </h2>
                                <p class="price mt-auto">
                                    <c:choose>
                                        <c:when test="${not empty product.discountPrice}">
                                            <fmt:formatNumber value="${product.discountPrice}"
                                                              maxFractionDigits="0"/> đ
                                            <span class="old-price">
                                                <fmt:formatNumber value="${product.price}"
                                                                  maxFractionDigits="0"/> đ
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatNumber value="${product.price}"
                                                              maxFractionDigits="0"/> đ
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

            <nav class="d-flex justify-content-between align-items-center mt-4">
                <c:if test="${not products.first}">
                    <c:url var="prevUrl" value="/products">
                        <c:param name="keyword" value="${keyword}"/>
                        <c:if test="${not empty categoryId}">
                            <c:param name="categoryId" value="${categoryId}"/>
                        </c:if>
                        <c:param name="sort" value="${sort}"/>
                        <c:param name="page" value="${products.number - 1}"/>
                    </c:url>
                    <a href="${prevUrl}" class="btn btn-outline-danger">Trang trước</a>
                </c:if>

                <span>Trang ${products.number + 1} / ${products.totalPages}</span>

                <c:if test="${not products.last}">
                    <c:url var="nextUrl" value="/products">
                        <c:param name="keyword" value="${keyword}"/>
                        <c:if test="${not empty categoryId}">
                            <c:param name="categoryId" value="${categoryId}"/>
                        </c:if>
                        <c:param name="sort" value="${sort}"/>
                        <c:param name="page" value="${products.number + 1}"/>
                    </c:url>
                    <a href="${nextUrl}" class="btn btn-outline-danger">Trang sau</a>
                </c:if>
            </nav>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="../common/footer.jsp" %>
