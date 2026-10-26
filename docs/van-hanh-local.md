# Chạy StarShop với SQL Server

## Cài đặt lần đầu

Yêu cầu Java 17 trở lên, Maven và SQL Server có TCP/IP. Trong PowerShell tại thư mục project:

```powershell
./scripts/setup-sqlserver.ps1
./scripts/start-local.ps1
```

Script thiết lập dùng tài khoản Windows hiện tại để tạo database **StarShopLocal**, tài khoản SQL riêng **starshop_local_app** chỉ được cấp quyền trong database này, JWT secret và mật khẩu Admin ngẫu nhiên. Không dùng lại database của bài tập khác. Script dừng nếu đã có cấu hình/login để giữ nguyên mật khẩu cũ.

Kết nối mặc định: SQL Express trên `127.0.0.1:1433`. Chứng chỉ tự ký được chấp nhận cho kết nối phát triển cục bộ; khi triển khai phải dùng chứng chỉ tin cậy và cấu hình kết nối phù hợp. Ứng dụng mặc định chỉ lắng nghe localhost. Tài khoản SQL có quyền sửa schema vì Hibernate dùng `ddl-auto=update` trong môi trường học tập; khi triển khai cần tách quyền migration khỏi tài khoản ứng dụng.

File `.local/application-local.properties` chứa tài khoản DB và Admin, bị loại khỏi Git. Mở file trên máy để xem tài khoản đăng nhập, không gửi nội dung file qua chat. Các lần chạy sau chỉ dùng `start-local.ps1`. Dữ liệu SQL Server được giữ khi đóng/mở ứng dụng. Hibernate cập nhật schema, không tạo lại database.

Nếu chạy từ Eclipse, thêm vào Program arguments: `--spring.config.additional-location=file:./.local/application-local.properties` và đặt Working directory là thư mục project StarShop. Không chạy hai server cùng cổng 8080.

Script chạy sao chép WAR sang `.local/starshop-runtime.war` để Maven có thể đóng gói lại trong `target/` khi server đang mở trên Windows. Trước khi chạy bản mới vẫn phải dừng server cũ; script kiểm tra cổng 8080 để báo rõ khi có tiến trình đang dùng.

Danh mục 50 sản phẩm được nạp lần đầu và không nhân đôi khi chạy lại. Không có đơn giả được tự tạo trong database SQL này.

## SMTP và Cloudinary

Điền tài khoản của mình trong `.local/services.properties`; file này cũng bị loại khỏi Git. Mẫu không chứa bí mật nằm ở `config/services.example.properties`.

- SMTP: host, port, username, password ứng dụng của nhà cung cấp. Gmail thường dùng cổng 587, STARTTLS; không dùng mật khẩu đăng nhập thông thường nếu nhà cung cấp yêu cầu app password.
- Cloudinary: cloud name, API key và API secret. Upload ảnh sản phẩm, ảnh/video đánh giá dùng các cấu hình này.
- Lưu file rồi khởi động lại ứng dụng. Khi chưa điền, website vẫn mở được nhưng không gửi OTP hoặc upload Cloudinary thật.
- Ảnh danh mục có sẵn phục vụ local, độc lập với chức năng upload Cloudinary.

## Phân quyền chuỗi cửa hàng

1. Admin cấp vai trò MANAGER cho tài khoản đã kích hoạt ở trang người dùng.
2. Admin vào chi tiết cửa hàng → Quản lý phụ trách → chọn tài khoản → Lưu phân công.
3. Manager chỉ thấy cửa hàng được giao, sản phẩm, đơn hàng và báo cáo tương ứng. Nhập trực tiếp ID cửa hàng khác cũng bị chặn.
4. Admin xem toàn bộ và có thể thay đổi/gỡ phân công. Manager chưa được giao cửa hàng sẽ thấy danh sách rỗng.
5. Danh mục, nhà vận chuyển, khuyến mãi cấp ứng dụng và quản lý user tiếp tục theo quyền vận hành chung trong đề bài; cấp quyền tài khoản vẫn dành riêng Admin.

## Template giao diện

Toàn bộ khu vực sử dụng Bootswatch **Litera 5.3.3**, dựa trên Bootstrap, qua tag dùng chung `/WEB-INF/tags/theme.tag`. CSS, Bootstrap JS và giấy phép MIT lưu trong `static/assets/vendor/`. `starshop-theme.css` thống nhất màu sắc, form, card và responsive. Không sửa HTML nghiệp vụ để thay thư viện bằng phiên bản không tương thích. Các khu vực Vendor/Shipper dùng thêm tag `workspaceNav.tag`; SiteMesh tiếp tục trang trí quản trị.

Bootstrap Icons 1.11.0 và Chart.js 4.4.8 cũng được lưu local cùng giấy phép; không cần CDN để tải icon/biểu đồ.

Nguồn thư viện giao diện: https://bootswatch.com/litera/ . Các thông tin kỹ thuật này chỉ nằm trong tài liệu, không đưa vào màn hình mua hàng.

## Kịch bản nghiệm thu

- Đăng ký → nhận email OTP → kích hoạt → đăng nhập; quên mật khẩu → OTP → đặt lại mật khẩu.
- Mua hai sản phẩm từ hai shop → hai đơn độc lập → voucher/tiền ship chính xác → giỏ được xóa sau thành công.
- Vendor xác nhận và chuẩn bị hàng → Manager phụ trách phân công → Shipper nhận/lấy/giao → User đánh giá ít nhất 50 ký tự, ảnh/video lên Cloudinary.
- Hủy và trả hàng → hoàn tồn kho đúng một lần; hoàn tiền COD là ghi nhận nghiệp vụ, không tự chuyển tiền qua ngân hàng.
- Manager thử ID sản phẩm/đơn hàng ngoài phạm vi → bị chặn; Admin vẫn xử lý được.
- Đóng/mở server → sản phẩm, tài khoản, đơn hàng SQL vẫn còn; thử màn hình điện thoại ở từng vai trò.
