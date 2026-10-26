# Nạp 50 mẫu hoa vào StarShop

Danh mục có 50 sản phẩm, chia đều 5 danh mục, mỗi sản phẩm có một ảnh WebP trên Cloudinary và bản gốc lưu trong project. Tên và giá nằm trong `src/main/resources/demo/vuonhoatuoi-catalog.json`; ảnh nằm trong `src/main/resources/static/images/catalog/vuonhoatuoi/`.

Sau khi cấu hình database và tạo tài khoản chủ shop, chạy ứng dụng với:

```text
--spring.profiles.active=demo
--starshop.demo-catalog.enabled=true
--starshop.demo-catalog.owner-email=admin@starshop.local
```

Thay email bằng tài khoản chủ shop thực tế. Importer tạo shop `StarShop Central` nếu chưa có, thêm danh mục và sản phẩm. Chạy lại không tạo sản phẩm trùng tên trong cùng shop. Không xóa hay ghi đè dữ liệu đang có. Khi không bật các tham số trên, ứng dụng không tự nạp.

Ảnh được phục vụ qua HTTPS từ Cloudinary, không lấy trực tiếp từ website cung cấp mẫu. Danh mục lưu URL ảnh và public ID; bản gốc WebP vẫn được giữ trong project. Tồn kho khởi tạo 30 và số đã bán ban đầu phục vụ việc kiểm tra giao diện. Giao diện sản phẩm chỉ hiện tên, ảnh, giá và mô tả.
