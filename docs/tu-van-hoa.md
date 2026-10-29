# Tư vấn chọn hoa

Khung tư vấn ở góc dưới các trang mua sắm. Khách có thể hỏi theo dịp, ngân sách hoặc màu sắc, rồi mở sản phẩm từ liên kết bên dưới câu trả lời.

Backend gọi Gemini bằng cấu hình dùng chung trong `src/main/resources/gemini-team.properties`. Có thể thay khóa bằng biến môi trường `GEMINI_API_KEY` hoặc file `.local/gemini.properties`; project tự đọc file này khi khởi động. File mẫu nằm tại `config/gemini.example.properties`. Không đưa khóa vào JavaScript, JSP hoặc log. Tắt chức năng bằng `starshop.gemini.enabled=false`.

Mỗi lượt gửi câu hỏi và tối đa ba cặp hội thoại gần nhất cùng danh mục công khai còn hàng đến Google. Không truy vấn tài khoản, địa chỉ, đơn hàng hoặc thông tin thanh toán. Hội thoại lưu trong session máy chủ. Đầu vào tối đa 1.000 ký tự. Sản phẩm gợi ý được kiểm tra lại theo danh mục đã gửi, bỏ ID không tồn tại và trùng lặp. Giao diện hiển thị văn bản bằng `textContent`.

Giữ CSRF cho POST `/flower-chat`. Mỗi session chờ ít nhất 10 giây giữa hai lượt, chỉ xử lý một lượt đồng thời. Giới hạn toàn tiến trình tối đa 15 lượt/phút và 500 lượt/ngày, tính cả lần gọi lỗi; bộ đếm ngày theo múi giờ Pacific. Bộ đếm trong bộ nhớ sẽ đặt lại khi khởi động ứng dụng; hạn mức thực tế của Google áp dụng theo project và vẫn là giới hạn cuối cùng. Không tự đổi sang dịch vụ trả phí khi hết hạn mức. Cấu hình hạn mức lớn hơn bị chặn ở 15/500.

Giá và trạng thái sản phẩm có thể thay đổi trong lúc tư vấn; trang sản phẩm và checkout xác nhận thông tin cuối cùng. Trợ lý không tự đặt đơn, thu tiền hoặc xác nhận tình trạng giao hàng. Danh mục tham khảo giới hạn 60 mẫu đang bán; khi mở rộng dữ liệu cần thêm tìm kiếm theo nhu cầu trước khi gửi.

Kiểm tra: CSRF, khách chưa đăng nhập, câu hỏi quá dài, thời gian chờ, danh mục rỗng, giá giảm, ID giả/trùng và hạn mức khi nhà cung cấp lỗi. Kiểm tra thực tế qua web local với cấu hình riêng; không sử dụng khóa thật trong test tự động.
