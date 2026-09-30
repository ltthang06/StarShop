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

    <title>
        ${editing ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm'} - StarShop
    </title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <style>
        .product-image-card {
            width: 160px;
        }

        .product-image-preview {
            width: 100%;
            height: 140px;
            object-fit: cover;
            border-radius: 8px;
        }
    </style>

</head>

<body class="bg-light">

<div class="container py-5">

    <div class="row justify-content-center">

        <div class="col-lg-9">

            <div class="card shadow-sm">

                <div class="card-body p-4">

                    <h2 class="mb-1">
                        ${editing
                            ? 'Chỉnh sửa sản phẩm'
                            : 'Thêm sản phẩm'}
                    </h2>

                    <p class="text-muted mb-4">
                        ${shop.name}
                    </p>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger">
                            ${errorMessage}
                        </div>
                    </c:if>

                    <c:if test="${not empty warningMessage}">
                        <div class="alert alert-warning">
                            ${warningMessage}
                        </div>
                    </c:if>

                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success">
                            ${successMessage}
                        </div>
                    </c:if>

                    <c:if test="${editing}">

                        <h5 class="mb-3">
                            Ảnh hiện tại
                        </h5>

                        <c:choose>

                            <c:when test="${not empty productImages}">

                                <div class="d-flex flex-wrap gap-3 mb-4">

                                    <c:forEach items="${productImages}"
                                               var="image">

                                        <div class="card product-image-card">

                                            <div class="card-body p-2">

                                                <img src="${image.imageUrl}"
                                                     class="product-image-preview"
                                                     alt="Ảnh sản phẩm">

                                                <c:if test="${image.primaryImage}">
                                                    <div class="mt-2">
                                                        <span class="badge bg-success">
                                                            Ảnh chính
                                                        </span>
                                                    </div>
                                                </c:if>

                                                <form method="post"
                                                      class="mt-2"
                                                      action="${pageContext.request.contextPath}/vendor/shops/${shop.id}/products/${productId}/images/${image.id}/delete">

                                                    <input type="hidden"
                                                           name="${_csrf.parameterName}"
                                                           value="${_csrf.token}">

                                                    <button type="submit"
                                                            class="btn btn-outline-danger btn-sm w-100"
                                                            onclick="return confirm('Bạn có chắc muốn xóa ảnh này?');">
                                                        Xóa ảnh
                                                    </button>

                                                </form>

                                            </div>

                                        </div>

                                    </c:forEach>

                                </div>

                            </c:when>

                            <c:otherwise>

                                <div class="alert alert-secondary mb-4">
                                    Sản phẩm chưa có ảnh.
                                </div>

                            </c:otherwise>

                        </c:choose>

                    </c:if>

                    <form method="post"
                          enctype="multipart/form-data"
                          action="${pageContext.request.contextPath}${formAction}">

                        <input type="hidden"
                               name="${_csrf.parameterName}"
                               value="${_csrf.token}">

                        <div class="mb-3">

                            <label class="form-label">
                                Tên sản phẩm
                            </label>

                            <input type="text"
                                   name="name"
                                   value="${productRequest.name}"
                                   class="form-control"
                                   maxlength="200"
                                   required>

                        </div>

                        <div class="mb-3">

                            <label class="form-label">
                                Mô tả
                            </label>

                            <textarea name="description"
                                      class="form-control"
                                      maxlength="2000"
                                      rows="5"
                                      required>${productRequest.description}</textarea>

                        </div>

                        <div class="row">

                            <div class="col-md-6 mb-3">

                                <label class="form-label">
                                    Giá bán (VNĐ)
                                </label>

                                <input type="number"
                                       name="price"
                                       value="${productRequest.price}"
                                       class="form-control"
                                       min="1000"
                                       step="1000"
                                       required>

                            </div>

                            <div class="col-md-6 mb-3">

                                <label class="form-label">
                                    Giá khuyến mãi (VNĐ)
                                </label>

                                <input type="number"
                                       name="discountPrice"
                                       value="${productRequest.discountPrice}"
                                       class="form-control"
                                       min="0"
                                       step="1000">

                            </div>

                        </div>

                        <div class="row">

                            <div class="col-md-4 mb-3">

                                <label class="form-label">
                                    Số lượng
                                </label>

                                <input type="number"
                                       name="quantity"
                                       value="${productRequest.quantity}"
                                       class="form-control"
                                       min="0"
                                       required>

                            </div>

                            <div class="col-md-4 mb-3">

                                <label class="form-label">
                                    Danh mục
                                </label>

                                <select name="categoryId"
                                        class="form-select"
                                        required>

                                    <option value="">
                                        Chọn danh mục
                                    </option>

                                    <c:forEach items="${categories}"
                                               var="category">

                                        <option value="${category.id}"
                                            ${productRequest.categoryId
                                                == category.id
                                                ? 'selected'
                                                : ''}>
                                            ${category.name}
                                        </option>

                                    </c:forEach>

                                </select>

                            </div>

                            <div class="col-md-4 mb-3">

                                <label class="form-label">
                                    Trạng thái
                                </label>

                                <select name="status"
                                        class="form-select">

                                    <c:forEach items="${statuses}"
                                               var="status">

                                        <option value="${status}"
                                            ${productRequest.status
                                                == status
                                                ? 'selected'
                                                : ''}>
                                            ${status}
                                        </option>

                                    </c:forEach>

                                </select>

                            </div>

                        </div>

                        <div class="mb-4">

                            <label class="form-label">
                                Ảnh sản phẩm
                            </label>

                            <input type="file"
                                   name="images"
                                   class="form-control"
                                   accept="image/*"
                                   multiple>

                            <div class="form-text">

                                Có thể chọn nhiều ảnh cùng lúc.
                                Ảnh đầu tiên của sản phẩm sẽ được dùng làm ảnh chính.

                                <c:if test="${editing}">
                                    Các ảnh mới sẽ được thêm vào những ảnh hiện có.
                                </c:if>

                            </div>

                        </div>

                        <button type="submit"
                                class="btn btn-primary">
                            Lưu sản phẩm
                        </button>

                        <a class="btn btn-secondary"
                           href="${pageContext.request.contextPath}/vendor/shops/${shop.id}/products">
                            Hủy
                        </a>

                    </form>

                </div>

            </div>

        </div>

    </div>

</div>

</body>
</html>