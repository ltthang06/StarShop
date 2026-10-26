# Báo cáo phần Guest/User — StarShop

Thành viên thực hiện: bổ sung họ tên, MSSV và các commit tương ứng trước khi nộp.

## 1. Phạm vi và tác nhân

Guest được xem trang chủ, tìm/lọc và xem chi tiết sản phẩm công khai. User được quản lý tài khoản, nhiều địa chỉ, mua hàng và theo dõi đơn của mình. Vendor kế thừa khả năng mua hàng của User. Sản phẩm công khai phải thuộc shop hoạt động, danh mục hoạt động và còn hàng.

## 2. Phân tích chức năng

| Nhóm | Luồng chính | Điều kiện và dữ liệu |
|---|---|---|
| Tài khoản | Đăng ký → email OTP → kích hoạt; đăng nhập/đăng xuất; quên mật khẩu bằng OTP | User, Role, OtpToken; mật khẩu BCrypt; OTP có thời hạn |
| Danh mục | Trang chủ, tìm tên/lọc danh mục, chi tiết, Top 20 | Product, Category, ProductImage; trang chủ lọc số bán >10 và giảm dần; Top 20 chia trang |
| Hồ sơ | Sửa thông tin, thêm/sửa/xóa địa chỉ | User, Address; xác minh địa chỉ thuộc chính tài khoản |
| Giỏ và thanh toán | Thêm/sửa/xóa giỏ → chọn địa chỉ/nhà vận chuyển/voucher → COD | Cart, CartItem, Order, OrderDetail; chia đơn theo shop, khóa tồn kho khi mua |
| Sau mua | Lọc lịch sử trạng thái, hủy/yêu cầu trả, đánh giá | Chỉ đơn của mình; chỉ đánh giá dòng sản phẩm đã giao, nội dung 50–2.000 ký tự |
| Tương tác | Yêu thích, đã xem, ảnh/video đánh giá | Wishlist, ProductView, Review; upload qua Cloudinary |

## 3. Thiết kế và triển khai

Controller nhận request và trả JSP; Service kiểm tra nghiệp vụ/transaction; Repository dùng JPA truy cập database. Các lớp chính là `CustomerAuthController`, `CustomerProductController`, `CustomerProfileController`, `CustomerCartController`, `CustomerCheckoutController`, `CustomerOrderController`, `CustomerReviewController` cùng các service tương ứng.

Giao diện nằm trong `views/auth`, `views/guest`, `views/customer`, dùng JSTL, Bootstrap/Litera và các tag định dạng tiền/trạng thái dùng chung. DTO được dùng cho card sản phẩm, tổng giỏ, đơn hàng và đánh giá.

Đặt hàng đọc giỏ từ database, kiểm tra shop/sản phẩm/danh mục, khóa sản phẩm theo thứ tự ID, kiểm tra số lượng, tính giá phía server, áp dụng voucher và tạo đơn theo shop trong transaction. Không tin tổng tiền phía trình duyệt. Giỏ được xóa sau khi tạo đơn; thông báo được phát sau commit.

## 4. Validation và bảo mật

Spring Security bảo vệ trang cần đăng nhập, các POST dùng CSRF. Service đối chiếu chủ sở hữu địa chỉ/đơn hàng/dòng đánh giá. Review chỉ cho đơn DELIVERED, điểm 1–5, nội dung 50–2.000 ký tự, định dạng và kích thước tệp được giới hạn. Không lưu mật khẩu rõ trong database. Email và Cloudinary thật cần cấu hình riêng của môi trường.

## 5. Kiểm tra và trình bày

Các lớp kiểm thử hiện có: `CustomerAuthFlowTests`, `CustomerProfileFlowTests`, `CustomerCartFlowTests`, `CustomerCheckoutFlowTests`, `CustomerActivityFlowTests`, `GuestPageTests`, `RankingPaginationTests`; luồng đánh giá/trả hàng liên vai trò còn nằm trong `ManagerCompletionTests`.

Khi báo cáo cần bổ sung ảnh chụp email OTP thật, đơn COD, giỏ database, trang mobile và ảnh/video đánh giá đã upload bằng tài khoản dịch vụ thật. Phân biệt kết quả kiểm thử H2 với lần nghiệm thu trên SQL Server. Không coi việc có giao diện là bằng chứng toàn bộ nghiệp vụ đã chạy thành công.

## 6. Câu hỏi cần nắm

- Vì sao chia đơn theo shop? Mỗi shop có người bán, xử lý và vận chuyển độc lập.
- Vì sao tính tiền trên server? Tránh người dùng sửa giá từ trình duyệt.
- Vì sao khóa sản phẩm khi mua? Tránh hai giao dịch cùng mua vượt tồn kho.
- Vì sao giỏ lưu DB? Giữ giỏ theo tài khoản giữa các lần đăng nhập.
- Vì sao chỉ người đã nhận hàng được đánh giá? Gắn nhận xét với giao dịch mua thực tế.
