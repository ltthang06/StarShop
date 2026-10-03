# Tiến độ Người 3 — Manager/Admin

## Đợt tích hợp ngày 03/10/2026

- Đồng bộ `develop` tại `97d725a` (Vendor/Shipper đã merge ngày 30/09).
- Người 1: nhánh Customer vẫn tại `c8d83d6`; chưa có luồng đặt hàng mới để tích hợp.
- Giữ cấu trúc Controller → Service → Repository và package chung `vn.iotstar.starshop`.

## Chức năng bổ sung

- `/manager/users`: tìm theo tên/email, phân trang, khóa/mở khóa tài khoản.
  Không tự khóa, không mở khóa tài khoản chưa kích hoạt; Manager không quản lý tài khoản Manager/Admin.
- `/manager/shipping-providers`: thêm/sửa nhà vận chuyển, phí cơ bản, số ngày dự kiến và trạng thái hoạt động.
- `/manager/orders`: lọc trạng thái, phân trang và phân công đơn `READY_FOR_PICKUP` cho Shipper đang hoạt động.
  Chặn phân công trùng; không thay đổi số tiền khách đã đặt.
- Luồng tích hợp với service của Người 2: `READY_FOR_PICKUP → ASSIGNED → PICKED_UP → SHIPPING → DELIVERED`.
- Sửa cấu hình Security phù hợp JSP và lỗi biểu thức EL ở trang shop.

## Chạy kiểm tra

```text
mvn verify
```

Test dùng H2, gồm phân quyền/CSRF, shop, khóa tài khoản, phân công và cập nhật giao hàng.
Test HTTP khởi động Tomcat, đăng nhập tài khoản Manager và render các trang JSP quản trị.
Chưa xác minh trên SQL Server thật hoặc upload Cloudinary thật.

Khi chạy ứng dụng với Cloudinary, đặt biến môi trường `CLOUDINARY_CLOUD_NAME`,
`CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`. Không đưa khóa thật vào Git.

## Các phần tiếp theo

- Quản lý sản phẩm từng shop, khuyến mãi cấp ứng dụng và thống kê quản trị.
- Tích hợp với luồng đặt hàng/đăng ký của Người 1 khi có code.
- Hoàn thiện JWT, WebSocket và báo cáo theo yêu cầu đề tài.

Nhánh làm việc: `feature/manager-admin`. Đợt này push nhánh cá nhân; việc merge vào `develop` thực hiện sau khi nhóm kiểm tra.
