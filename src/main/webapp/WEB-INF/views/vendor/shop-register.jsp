<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký cửa hàng - StarShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">

            <div class="card shadow-sm">
                <div class="card-body p-4">

                    <h2 class="mb-4">Đăng ký cửa hàng</h2>

                    <form method="post" action="${pageContext.request.contextPath}/vendor/shop/register">

                        <div class="mb-3">
                            <label class="form-label">Tên cửa hàng</label>
                            <input type="text"
                                   name="name"
                                   class="form-control"
                                   maxlength="150"
                                   required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Mô tả</label>
                            <textarea name="description"
                                      class="form-control"
                                      rows="4"
                                      maxlength="1000"></textarea>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Số điện thoại</label>
                            <input type="text"
                                   name="phone"
                                   class="form-control"
                                   maxlength="20">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Email</label>
                            <input type="email"
                                   name="email"
                                   class="form-control"
                                   maxlength="150">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Địa chỉ cửa hàng</label>
                            <input type="text"
                                   name="address"
                                   class="form-control"
                                   maxlength="255">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Logo URL</label>
                            <input type="text"
                                   name="logo"
                                   class="form-control">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Banner URL</label>
                            <input type="text"
                                   name="banner"
                                   class="form-control">
                        </div>

                        <button type="submit" class="btn btn-primary">
                            Đăng ký cửa hàng
                        </button>

                    </form>

                </div>
            </div>

        </div>
    </div>
</div>

</body>
</html>