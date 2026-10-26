# Cloudinary

Cấu hình riêng nằm trong `.local/services.properties`: `cloudinary.cloud-name`, `cloudinary.api-key`, `cloudinary.api-secret`. File này bị Git bỏ qua. Mẫu không chứa khóa nằm trong `config/services.example.properties`.

Vendor tải ảnh sản phẩm lên thư mục `starshop/products`. Khách đã nhận hàng có thể gửi ảnh/video kèm đánh giá; các tệp này lưu trong `starshop/reviews`. Đánh giá chỉ dành cho sản phẩm đã giao, nội dung tối thiểu 50 ký tự; ảnh JPEG/PNG/WebP và video MP4/WebM tối đa 10 MB mỗi tệp.

Ngày 26/10/2026: đã cấu hình tài khoản Cloudinary qua Google theo yêu cầu và upload thành công ảnh WebP cùng video MP4 bằng đúng service Java của StarShop. Cả hai URL HTTPS trả nội dung đúng loại ảnh/video. Kết quả nằm trong `target/cloudinary-live-result.json`, log `target/cloudinary-live-upload.log`; không tạo đánh giá thử trên storefront.

WebM được trình duyệt nhận theo nội dung trả về, không ép nhãn MP4. Chạy lại `scripts/start-local.ps1` khi thay đổi cấu hình. Không chia sẻ API secret hoặc mật khẩu SMTP trong source, log hay commit.

Toàn bộ 50 ảnh sản phẩm đã upload vào `starshop/products`. Đã kiểm tra 50/50 URL HTTPS trả về ảnh WebP hợp lệ và cập nhật `product_images` trên SQL Server. Danh mục trong Git lưu URL và public ID để các máy khác nhập được cùng bộ ảnh; khóa API vẫn nằm ngoài Git.
