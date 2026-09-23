<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>StarShop - Quản trị</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-dark bg-dark">
    <div class="container"><a class="navbar-brand" href="${pageContext.request.contextPath}/manager">StarShop Admin</a>
        <span class="text-white-50">Manager / Admin</span></div>
</nav>
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div><h1 class="h3 mb-1">Bảng điều khiển</h1><p class="text-muted mb-0">Khu vực quản lý hệ thống</p></div>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/manager/categories">Quản lý danh mục</a>
    </div>
    <div class="row g-3">
        <div class="col-md-4"><div class="card shadow-sm"><div class="card-body"><div class="text-muted">Danh mục</div><div class="display-6">${categoryCount}</div></div></div></div>
        <div class="col-md-4"><div class="card shadow-sm"><div class="card-body"><div class="text-muted">Shop chờ duyệt</div><div class="display-6">-</div><small class="text-muted">Sẽ nối với module Shop ở đợt tiếp theo</small></div></div></div>
        <div class="col-md-4"><div class="card shadow-sm"><div class="card-body"><div class="text-muted">Đơn hàng</div><div class="display-6">-</div><small class="text-muted">Sẽ nối với module Order ở đợt tích hợp</small></div></div></div>
    </div>
</main>
</body>
</html>
