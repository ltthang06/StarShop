# Kết quả hoàn thiện ngày 16/10/2026

## Đã thực hiện

- Chạy toàn ứng dụng với SQL Server thật, database riêng StarShopLocal, tài khoản ứng dụng và JWT secret riêng; cấu hình bí mật được giữ ngoài Git.
- Nạp 50 sản phẩm/50 ảnh vào SQL Server, kiểm tra giữ nguyên dữ liệu khi khởi động lại.
- Áp dụng chung Litera/Bootstrap 5.3.3 qua JSP tag; lưu CSS, JS, icon và thư viện biểu đồ trong project, giữ giấy phép thư viện.
- Admin phân công Manager phụ trách shop. Lọc danh sách, dữ liệu báo cáo và kiểm tra truy cập trực tiếp sản phẩm/đơn hàng/shop theo phạm vi Manager.
- Sửa điều hướng Vendor, bổ sung liên kết vai trò, responsive bảng và cải thiện tương phản chữ trên trang chủ.
- Có scripts setup/start/check local, mẫu cấu hình SMTP/Cloudinary, hướng dẫn Eclipse và nội dung báo cáo cho ba thành viên.

## Bằng chứng kiểm tra

- `mvn verify`: 27 tests, không failure/error. Log: `target/completion-final-verify.log`.
- Kiểm thử ManagerShopScopeTests: Manager không được xem/sửa shop ngoài phạm vi; không tự phân công; Admin truy cập và gỡ phân công được.
- Kiểm tra HTTP trên SQL Server với Admin: trang chủ, sản phẩm, Top 20, dashboard, user, shop, sản phẩm quản trị, đơn, báo cáo, voucher, vận chuyển, Vendor dashboard/shop, Shipper và thông báo trả thành công. Log: `target/sqlserver-smoke.log`.
- SQL Server kiểm tra 50 sản phẩm còn nguyên sau đóng/mở ứng dụng. Schema có cột `shops.manager_id`.

## Cần hoàn tất bằng tài khoản của người dùng

- Điền SMTP trong `.local/services.properties`, khởi động lại và nghiệm thu email OTP đăng ký/quên mật khẩu thật.
- Điền Cloudinary và nghiệm thu upload ảnh/video thật. Ảnh danh mục local không thay thế phép thử này.
- Điền họ tên/MSSV và thêm ảnh minh chứng vào báo cáo từng thành viên; nhóm cần luyện trình bày luồng theo vai trò.
- Chưa push GitHub khi chưa có xác nhận của người dùng.
