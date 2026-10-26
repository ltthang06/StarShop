<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<div id="flower-chat" data-endpoint="${pageContext.request.contextPath}/flower-chat" data-products="${pageContext.request.contextPath}/products/" data-csrf="${_csrf.token}" data-csrf-header="${_csrf.headerName}">
    <button id="flower-chat-toggle" type="button" aria-expanded="false" aria-controls="flower-chat-panel"><i class="bi bi-chat-heart" aria-hidden="true"></i> Tư vấn chọn hoa</button>
    <section id="flower-chat-panel" aria-label="Tư vấn chọn hoa StarShop" hidden>
        <div class="flower-chat-heading"><div><strong>StarShop · Tư vấn hoa</strong><small>Trợ lý tự động giúp bạn chọn hoa</small></div><button id="flower-chat-close" type="button" aria-label="Đóng tư vấn">×</button></div>
        <div id="flower-chat-messages" role="log" aria-live="polite" aria-relevant="additions"><div class="flower-chat-message">Chào bạn! Bạn muốn tặng hoa dịp gì và ngân sách khoảng bao nhiêu?</div></div>
        <form id="flower-chat-form"><label class="visually-hidden" for="flower-chat-input">Câu hỏi tư vấn</label><textarea id="flower-chat-input" maxlength="1000" rows="2" placeholder="Ví dụ: Hoa sinh nhật mẹ dưới 700.000đ" required></textarea><button type="submit" id="flower-chat-send">Gửi</button></form>
        <p class="flower-chat-note">Giá và tình trạng cuối cùng được xác nhận khi đặt hàng.</p>
    </section>
</div>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/flower-chat.css">
<script src="${pageContext.request.contextPath}/js/flower-chat.js" defer></script>
