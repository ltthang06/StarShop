# Tích hợp và sửa lỗi ngày 16/10/2026

## Nguồn code

Đã fetch và đối chiếu các nhánh: develop/Customer tại `72a7661`, Vendor/Shipper tại `c946c74`.
Nhánh `feature/manager-admin` đã chứa các commit này; không có commit mới cần merge.
Các sửa đổi dưới đây được thực hiện trên nền `f22913b`.

## Sửa lỗi và hoàn thiện

- Top 20: chia 8 sản phẩm/trang, giữ tối đa 20 sản phẩm, giữ kiểu xếp hạng và đánh số liên tục; xử lý page âm/quá lớn.
- Lịch sử mua hàng: lọc theo trạng thái, giữ bộ lọc khi chuyển trang và chỉ trả đơn của tài khoản hiện tại.
- Giữ các chữ ký service cũ để code của các thành viên và test hiện có tiếp tục hoạt động.
- Hiển thị tiền Việt thống nhất bằng formatter dùng dấu chấm phân nhóm, độc lập locale của JDK.
- Các trạng thái quản trị và lịch sử đơn của User được hiển thị bằng tiếng Việt; giữ nguyên enum/database/API.
- Menu quản trị có nút đóng/mở trên điện thoại, đánh dấu mục đang xem; dùng CSS/JS riêng tại `static/css/manager.css` và `static/js/manager.js`.
- Quản trị sản phẩm: chọn shop theo tên, ảnh đại diện, liên kết xem đầy đủ ảnh/mô tả, giá, tồn kho và kiểm duyệt.
- Chi tiết đơn quản trị: thông tin người nhận, sản phẩm/đơn giá/số lượng, cửa hàng, vận chuyển, Shipper, thanh toán và giảm giá.
- Không còn hiển thị nút khóa không hợp lệ cho chính tài khoản đăng nhập hoặc tài khoản được bảo vệ.
- URL quản trị có ID không tồn tại trả 404; bộ lọc không hợp lệ trả 400 kèm thông báo dễ hiểu.

## Kiểm tra

Lệnh kiểm tra toàn bộ: `mvn verify`.

Test bổ sung kiểm tra:
- Dữ liệu trên 20 sản phẩm vẫn chỉ có 20 vị trí xếp hạng, không lặp hoặc bỏ hạng qua các trang.
- Lịch sử đơn theo trạng thái không lộ đơn của người khác.
- Chi tiết đơn trả đúng dòng sản phẩm và Shipper đã phân công.
- HTTP/Tomcat render các trang mới, tiền Việt, trang lỗi 404 và bộ lọc 400.
- Các test JWT, CSRF, WebSocket và luồng Customer → Vendor → Manager → Shipper cũ tiếp tục chạy.

## Phạm vi xác minh

Ứng dụng xem trước tại `http://127.0.0.1:8080` dùng H2 và dữ liệu mẫu, không phải database SQL Server triển khai.
Khởi động lại bản xem trước tạo lại dữ liệu mẫu.
Chưa xác minh SMTP, upload Cloudinary và toàn ứng dụng qua JDBC SQL Server thật.
Chưa thay đổi mô hình Manager quản lý toàn hệ thống thành Manager được gán từng cửa hàng.

