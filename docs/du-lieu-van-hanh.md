# Dữ liệu vận hành StarShop

## Khởi tạo trên máy mới

Sau khi thiết lập SQL Server và danh mục 50 sản phẩm, chạy:

```powershell
./scripts/prepare-showcase.ps1
./scripts/start-local.ps1
```

Mật khẩu tài khoản được sinh ngẫu nhiên, lưu trong `.local/accounts.md` và cấu hình `.local/showcase.properties`. Các file này bị loại khỏi Git. Script không ghi đè tài khoản đã tồn tại hoặc đổi mật khẩu của người đang dùng. Tài khoản khởi tạo được kích hoạt để kiểm tra nghiệp vụ; luồng kích hoạt qua email được nghiệm thu riêng khi có SMTP.

## Nội dung

| Nhóm | Dữ liệu |
|---|---|
| Cửa hàng | StarShop Central, StarShop Thủ Đức, StarShop Bình Thạnh |
| Sản phẩm | Tổng 50 mẫu, phân bổ 17/17/16 giữa ba cửa hàng, giữ ảnh và giá |
| Tài khoản | Admin hiện có, 2 Vendor, 2 Manager, 1 Shipper, 1 User |
| Phân quyền | Manager Thủ Đức phụ trách Central/Thủ Đức; Manager Bình Thạnh phụ trách Bình Thạnh |
| Vận chuyển | StarShop Express 25.000đ; StarShop Standard 18.000đ |
| Voucher | HOATUOI10 giảm 10%, FREESHIP giảm vận chuyển, THUDUC50 giảm 50.000đ cho shop Thủ Đức |
| Đơn hàng | 13 đơn qua service, bao phủ mới/xác nhận/sẵn sàng/phân công/lấy hàng/đang giao/đã giao/hủy/yêu cầu trả/đã trả/hoàn tiền |
| Khách hàng | Hai địa chỉ, yêu thích, đã xem và một đánh giá đủ 50 ký tự |
| Chiết khấu | Khởi tạo 5% cho mỗi cửa hàng |

Đơn được tạo bằng giỏ hàng/checkout, chuyển trạng thái bằng các service Vendor, Manager, Shipper, User. Ca giỏ hàng hai shop xác minh tách thành hai đơn và tổng tiền đúng với báo giá sau voucher. Không gọi dịch vụ thanh toán thật, không gửi yêu cầu giao hàng ra đơn vị bên ngoài. COD/hoàn tiền là ghi nhận nghiệp vụ trong ứng dụng.

Sau khi hoàn tất, đặt `starshop.showcase.enabled=false` và `starshop.showcase.orders-enabled=false` trong file riêng để lần mở tiếp theo không tự thiết lập lại. Danh mục dùng mã ảnh đã nhập để không nạp trùng sau khi phân sản phẩm sang shop khác. Chạy lại bộ thiết lập trong kiểm thử không tạo thêm tài khoản/shop/đơn trùng.

## Kiểm tra

```powershell
./scripts/check-local.ps1
./scripts/check-showcase.ps1
```

Script đăng nhập từng tài khoản local, kiểm tra các trang theo vai trò, User/Shipper không vào được quản trị và Manager không thấy chi nhánh ngoài phạm vi. Không in mật khẩu. Báo cáo mỗi thành viên cần bổ sung thông tin cá nhân và ảnh chụp luồng tương ứng trước khi nộp.

## Kết quả trên SQL Server ngày 23/10/2026

- Khởi tạo qua service thành công, kể cả tách giỏ hai shop và đối chiếu tổng tiền sau voucher.
- SQL xác nhận 50 sản phẩm, 3 cửa hàng, 13 đơn, 7 tài khoản, 2 nhà vận chuyển, 3 voucher và 1 đánh giá.
- Tắt hai cờ khởi tạo, đóng/mở ứng dụng: các số lượng trên giữ nguyên, không nạp lại 50 sản phẩm sau khi đổi shop.
- Đăng nhập đủ 6 tài khoản mới và Admin, kiểm tra trang theo vai trò và ranh giới quản trị/chi nhánh thành công. Log: `target/showcase-roles-smoke.log`, `target/showcase-admin-smoke.log`.
- `mvn verify`: 44 tests, không failure/error; log `target/showcase-final-verify.log`. `ShowcaseSetupTests` kiểm tra số lượng, trạng thái và chạy lại không tạo bản ghi trùng.
- Email OTP và upload ảnh/video thật tiếp tục cần cấu hình SMTP/Cloudinary trong `.local/services.properties`.
