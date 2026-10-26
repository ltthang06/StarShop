# VNPAY Sandbox

## Cấu hình

Thông tin merchant nằm trong `.local/vnpay.properties`, được loại khỏi Git. Không đưa HashSecret vào source, JSP, log hoặc JavaScript. Mẫu cấu hình không chứa khóa nằm ở `config/vnpay.example.properties`. File cấu hình local chính nhập file VNPAY khi chạy ứng dụng.

- `starshop.vnpay.enabled=true` để hiển thị phương thức VNPAY ở checkout.
- `tmn-code`, `hash-secret` dùng thông tin cấp riêng từ VNPAY.
- `return-url` là địa chỉ trình duyệt khách quay về sau thanh toán.
- Kết nối hiện chỉ hỗ trợ Sandbox tại `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`; không dùng cho tiền thật.

## Luồng xử lý

1. Checkout chọn COD hoặc VNPAY. VNPAY tạo đơn và giao dịch trong cùng transaction; lỗi tạo giao dịch sẽ rollback đơn/tồn kho/giỏ.
2. Một giao dịch chứa tổng tiền của các đơn theo shop. Giá lấy từ database, không nhận số tiền do trình duyệt gửi.
3. Trang giao dịch của chủ tài khoản dẫn sang VNPAY bằng URL ký HMAC-SHA512, thời hạn 15 phút, thời gian GMT+7.
4. Return URL kiểm tra chữ ký, số tiền và chủ giao dịch, ghi nhớ kết quả đã nhận để ẩn/chặn mở thanh toán lại. Return thành công được kiểm tra chữ ký, chủ sở hữu, Merchant, số tiền, mã giao dịch và trạng thái 00/00 rồi tự ghi nhận PAID qua cùng hàm cập nhật dùng cho IPN; gọi lặp không cập nhật lần hai.
5. IPN xác minh chữ ký, mã merchant, tham chiếu, số tiền và kết quả. Khóa bản ghi giao dịch rồi các đơn để cập nhật đúng một lần. Giao dịch thành công ghi PAID cho từng đơn; thất bại cuối cùng hủy đơn và hoàn kho một lần.
6. Kết quả đang xử lý tiếp tục chờ. Giao dịch nghi ngờ/đảo hoặc thông báo mâu thuẫn chuyển sang REVIEW_REQUIRED để đối soát, không thông báo thành công hoặc tự hoàn kho sai.

Lịch sử đơn có phương thức/trạng thái thanh toán và liên kết tới giao dịch. Vendor không xác nhận đơn VNPAY chưa PAID. Luồng COD giữ nguyên. Không cho hủy đơn VNPAY qua nút hủy COD để tránh xung đột với giao dịch đang xử lý. Đơn quá hạn chưa từng mở cổng được hủy và hoàn kho sau khoảng đệm 2 phút. Đơn đã mở cổng cần đối soát API querydr; chỉ kết quả thất bại cuối cùng đã xác minh mới được hoàn kho. Mất kết nối, không tìm thấy giao dịch, phản hồi không hợp lệ hoặc còn đang xử lý đều giữ nguyên tồn kho và ghi chú để Admin kiểm tra.

Hoàn tiền VNPAY qua API chưa được triển khai; ứng dụng chặn việc bấm nút rồi tự ghi REFUNDED. Merchant cần xử lý/đối soát hoàn tiền trên cổng thanh toán. Không coi trạng thái hoàn tiền COD là một giao dịch hoàn tiền VNPAY thật.

## IPN công khai

Đường dẫn nhận IPN: `/payments/vnpay/ipn` (GET), trả JSON `RspCode`/`Message`. Endpoint không yêu cầu đăng nhập nhưng mọi kết quả phải có chữ ký hợp lệ. Các trang giao dịch khác yêu cầu đăng nhập và đúng chủ sở hữu.

VNPAY không thể gọi `127.0.0.1` hoặc `localhost` của máy cá nhân. Để kiểm tra trọn luồng:

1. Chuẩn bị địa chỉ HTTPS cho ứng dụng hoặc relay HTTPS chỉ chuyển tiếp IPN tới endpoint local.
2. Cấu hình URL IPN công khai trong merchant theo hướng dẫn VNPAY; không gửi HashSecret vào URL.
3. Nếu trình duyệt dùng domain mới, sửa `return-url` tương ứng và khởi động lại.
4. Dùng thẻ thử nghiệm do VNPAY cung cấp; xác nhận giao dịch trên gateway, kiểm tra IPN cập nhật database. Không dùng thẻ thật trong môi trường này.

Máy local có thể dùng relay chỉ mở IPN qua Cloudflare Tunnel khi được yêu cầu; các trang đăng nhập/quản trị không cần công khai. Khi chưa có IPN công khai, kiểm thử chữ ký/callback chạy được tại máy nhưng chưa chứng minh VNPAY đã gọi IPN thật. Không giả lập cập nhật PAID từ Return URL để thay bước này.

## Kiểm thử

`VnpayPaymentTests` kiểm tra gộp đơn nhiều shop, Return không đổi trạng thái, IPN thành công/lặp/song song, sửa số tiền, sai merchant/chữ ký, tham số trùng, thất bại hoàn kho một lần, giao dịch nghi ngờ, hết hạn URL, quyền sở hữu và rollback khi thiếu cấu hình. Khóa trong test là khóa giả độc lập với merchant local.

Tài liệu giao thức: https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html

## Kết quả kiểm tra ngày 24/10/2026

- Bộ kiểm thử toàn dự án có 52 tests đạt, gồm 8 tests VNPAY. Log `target/vnpay-unicode-verify.log`.
- Merchant local tạo URL thanh toán và được trang VNPAY Sandbox chấp nhận, hiển thị màn hình chọn phương thức thanh toán Test. Chưa nhập thẻ hoặc thực hiện giao dịch ngân hàng.
- Một giao dịch local và đơn #14 được tạo qua checkout trên SQL Server, vẫn PENDING. Không giả lập IPN thành công bằng khóa merchant để đổi trạng thái.
- IPN không có chữ ký hợp lệ trả RspCode 97. Kiểm tra trang quản trị và Customer trên SQL Server thành công; log `target/vnpay-sqlserver-smoke.log`.
- Sửa các cột tên/địa chỉ/nội dung đánh giá/thông báo sang Unicode bằng mapping và migration SQL Server chạy trước bước đọc tài khoản. Các bản ghi khởi tạo bị mất dấu được khôi phục từ giá trị đã biết, không suy đoán dữ liệu người dùng khác. Tên/địa chỉ trên hồ sơ đã hiển thị đúng dấu.
- Kiểm tra khóa merchant không xuất hiện trong source/tài liệu/cấu hình chia sẻ. File riêng `.local/vnpay.properties` tiếp tục bị loại khỏi Git.
- Chưa có địa chỉ IPN công khai nên chưa nghiệm thu callback từ máy chủ VNPAY. Chưa triển khai hoàn tiền qua API VNPAY, chưa bật thanh toán tiền thật, chưa push GitHub.

## Đối soát và xử lý quá hạn

- `starshop.vnpay.reconciliation-enabled=true` bật lịch kiểm tra sau khi ứng dụng chạy 1 phút, mỗi đợt tối đa 10 giao dịch, mỗi giao dịch tự kiểm tra lại cách nhau ít nhất 5 phút. Tắt mặc định trong mẫu cấu hình để test tự động không gọi mạng.
- Nút **Kiểm tra với VNPAY** trên trang giao dịch gửi POST có CSRF; chỉ chủ giao dịch được sử dụng, giới hạn tra cứu tối thiểu 60 giây/lần.
- API querydr chính thức ở `https://sandbox.vnpayment.vn/merchant_webapi/api/transaction`; xác minh HMAC theo danh sách trường của querydr, mã Merchant, tham chiếu, loại giao dịch thanh toán và số tiền trước khi cập nhật. Phản hồi lỗi API hoặc thiếu chữ ký không dùng để hủy đơn.
- Admin mở **Đơn hàng → Đối soát thanh toán VNPAY** hoặc menu **Đối soát thanh toán** để xem hàng đợi, ghi chú và tra cứu lại. Giao dịch thuộc Merchant cũ/không rõ Merchant không dùng khóa mới để tự hủy; cần đối chiếu bằng tài khoản cũ.
- Không giữ khóa database trong lúc chờ HTTP từ VNPAY. Khi áp dụng kết quả, khóa lại giao dịch/đơn; IPN đã xác nhận trước thì kết quả tra cứu cũ không được ghi đè hoặc hoàn kho.
- Cột mới đều nullable để giữ lịch sử cũ. Merchant của giao dịch cũ chỉ được bổ sung khi nhận Return có chữ ký hợp lệ; không suy đoán Merchant từ cấu hình hiện tại.
- Tài liệu API tra cứu: https://sandbox.vnpayment.vn/apis/docs/truy-van-hoan-tien/querydr%26refund.html

## Kiểm tra bản local ngày 26/10/2026

68 kiểm thử toàn dự án đạt, gồm 17 kiểm thử VNPAY. Log: `target/vnpay-reconcile-verify.log`. Bản chạy SQL Server đã kiểm tra trang thanh toán, lưu Return hợp lệ của đơn #18, chặn mở lại thanh toán và hiển thị trang Admin đối soát. Lần truy vấn thật của đơn #18 chưa nhận được kết quả được xác minh nên giữ PENDING và tồn kho. Không ghi PAID bằng Return hoặc gửi lại IPN trong lượt này. Theo phạm vi đồ án cơ bản, cấu hình local tắt lịch đối soát tự động (`reconciliation-enabled=false`); khách/Admin chủ động bấm kiểm tra. Không push GitHub.

## Chế độ tự động

Return có chữ ký VNPAY hợp lệ và trạng thái thành công tự xác nhận thanh toán khi khách quay về web, không cần bấm kiểm tra hoặc ghi nhận lại IPN. IPN vẫn hoạt động độc lập và idempotent. Tra cứu định kỳ được bật trên máy local làm dự phòng; nếu khách đóng trang trước khi Return/IPN đến thì vẫn cần API tra cứu trả kết quả hợp lệ. Trang thanh toán đang chờ tự kiểm tra trạng thái mỗi 15 giây khi đang mở.

Với JDK trên Windows của máy local, thêm VM options `-Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore=NONE` để dùng kho chứng chỉ tin cậy của Windows. `scripts/start-local.ps1` đã có hai tùy chọn này. Không tắt kiểm tra HTTPS.

Kiểm tra cuối ngày 26/10/2026: 18 tests VNPAY đạt (`target/vnpay-return-auto-verify.log`). Khởi động bản mới bằng kho chứng chỉ Windows và chế độ kiểm tra định kỳ. Đã mở lại Return có chữ ký thật của giao dịch Sandbox 15715252; đơn #18 tự chuyển từ PENDING sang PAID qua handler Return, không sửa SQL hoặc gửi lại IPN. Ảnh `target/ui-review/vnpay-order18-auto-paid.png`. Chưa nghiệm thu IPN do VNPAY tự gọi; API querydr từng trả mã 94 (Request is duplicated), vì vậy khi khách đóng trình duyệt trước Return thì chưa khẳng định mọi giao dịch tự đối soát được. Chưa push GitHub.
