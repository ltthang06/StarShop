# Báo cáo phần Vendor/Shipper — StarShop

Thành viên thực hiện: bổ sung họ tên, MSSV và các commit tương ứng trước khi nộp.

## 1. Phạm vi

Vendor đăng ký shop, cập nhật thông tin/logo/banner, quản lý sản phẩm và ảnh, đơn hàng, voucher và doanh thu của shop mình. Shipper xem đơn được phân công, cập nhật tiến trình giao và thống kê số đơn. Vendor vẫn có chức năng mua hàng của User.

## 2. Phân tích chức năng

| Chức năng | Luồng xử lý và giới hạn |
|---|---|
| Đăng ký shop | User gửi thông tin → shop PENDING → quản trị duyệt → ACTIVE và cấp VENDOR |
| Trang shop | Chỉnh thông tin, ảnh; trang công khai chỉ hiển thị shop/sản phẩm hợp lệ |
| Sản phẩm | Thêm/sửa/trạng thái, tồn kho, giá/giá giảm, ảnh Cloudinary; kiểm tra quyền chủ shop |
| Đơn hàng | Xem/lọc đơn của shop, xác nhận, chuẩn bị hàng, hủy, nhận trả và ghi nhận hoàn tiền |
| Khuyến mãi | Voucher phạm vi shop, loại giảm giá, thời gian/số lượng/điều kiện áp dụng |
| Doanh thu | Thống kê theo thời gian, trạng thái, top sản phẩm; không nhầm doanh thu với số đơn |
| Shipper | Xem danh sách/chi tiết đúng phân công, lấy hàng, giao hàng, hoàn tất; thống kê theo trạng thái |

## 3. Thiết kế

Entity chính: Shop, Product, ProductImage, Promotion, Order, OrderDetail, ShipperAssignment, ShippingProvider. Controller chính: `VendorController`, `VendorProductController`, `VendorOrderController`, `VendorPromotionController`, `VendorStatisticController`, `PublicShopController`, `ShipperController`. Controller gọi service, service dùng repository và transaction.

JSP nằm trong `views/vendor`, `views/shipper`, `views/public`; dùng cùng Litera/Bootstrap và thanh điều hướng workspace. Upload sản phẩm, logo/banner và media dùng Cloudinary; cấu hình dịch vụ được tách khỏi Git.

## 4. Luồng đơn hàng tích hợp

`NEW → CONFIRMED → READY_FOR_PICKUP → ASSIGNED → PICKED_UP → SHIPPING → DELIVERED`.

User có thể yêu cầu trả từ đơn đã giao; Vendor nhận trả rồi ghi nhận hoàn tiền theo luồng `RETURN_REQUESTED → RETURNED → REFUNDED`. Hủy/trả phải hoàn lại tồn kho đúng một lần. COD được ghi nhận thanh toán khi giao thành công; trạng thái hoàn tiền không phải một giao dịch chuyển tiền ngân hàng tự động.

Manager phụ trách phân công đơn sẵn sàng lấy hàng; Shipper chỉ được cập nhật assignment của chính mình. Vendor không được tự sửa sản phẩm đang bị quản trị khóa để vượt kiểm duyệt. Dữ liệu doanh thu lọc theo shop, người bán không được truy cập shop khác bằng cách đổi ID URL.

## 5. Kiểm tra

`ManagerCompletionTests` có luồng liên vai trò từ đặt hàng đến giao/đánh giá và hoàn trả; `ManagerOperationsTests` kiểm tra phân công và giao hàng. Trước khi nộp cần minh họa hai tài khoản Vendor, hai shop độc lập, Shipper riêng, thử truy cập ID của shop/assignment khác và upload Cloudinary thật. Xem hướng dẫn môi trường tại `van-hanh-local.md`.

## 6. Câu hỏi cần nắm

- Vì sao kiểm tra owner trong service? Người dùng có thể tự thay ID trong request.
- Vì sao không cho nhảy từ NEW sang DELIVERED? Mỗi bước có tác nhân và điều kiện nghiệp vụ riêng.
- Vì sao phân công không được thay tổng tiền? Đó là số tiền khách đã xác nhận khi đặt.
- Vì sao trả hàng và hoàn tiền là hai bước? Nhận lại hàng và xác nhận xử lý tiền là hai sự kiện khác nhau.
- Vì sao phát WebSocket sau commit? Tránh thông báo thay đổi chưa được lưu thành công.
