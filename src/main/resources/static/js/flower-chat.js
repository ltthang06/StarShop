(() => {
    const root = document.getElementById('flower-chat');
    if (!root) return;
    const toggle = document.getElementById('flower-chat-toggle');
    const panel = document.getElementById('flower-chat-panel');
    const input = document.getElementById('flower-chat-input');
    const form = document.getElementById('flower-chat-form');
    const send = document.getElementById('flower-chat-send');
    const messages = document.getElementById('flower-chat-messages');
    const open = value => {
        panel.hidden = !value;
        toggle.setAttribute('aria-expanded', String(value));
        if (value) input.focus();
    };
    toggle.addEventListener('click', () => open(panel.hidden));
    document.getElementById('flower-chat-close').addEventListener('click', () => { open(false); toggle.focus(); });
    root.addEventListener('keydown', event => { if (event.key === 'Escape') open(false); });
    const append = (text, kind = '') => {
        const bubble = document.createElement('div');
        bubble.className = 'flower-chat-message ' + kind;
        bubble.textContent = text;
        messages.appendChild(bubble);
        while (messages.children.length > 30) messages.firstElementChild.remove();
        messages.scrollTop = messages.scrollHeight;
        return bubble;
    };
    form.addEventListener('submit', async event => {
        event.preventDefault();
        const message = input.value.trim();
        if (!message || send.disabled) return;
        append(message, 'customer');
        input.value = '';
        send.disabled = true;
        const pending = append('Đang chọn những mẫu hoa phù hợp cho bạn…');
        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 45000);
        try {
            const headers = { 'Content-Type': 'application/json' };
            if (root.dataset.csrfHeader && root.dataset.csrf) headers[root.dataset.csrfHeader] = root.dataset.csrf;
            const response = await fetch(root.dataset.endpoint, {
                method: 'POST', credentials: 'same-origin', headers,
                body: JSON.stringify({ message }), signal: controller.signal
            });
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || 'Phiên truy cập đã thay đổi. Bạn hãy tải lại trang và thử lại.');
            pending.textContent = data.reply;
            for (const product of data.products || []) {
                if (!Number.isSafeInteger(product.id) || product.id <= 0) continue;
                const link = document.createElement('a');
                link.className = 'flower-chat-product';
                link.href = root.dataset.products + product.id;
                link.textContent = product.name + ' · ' + Number(product.price).toLocaleString('vi-VN') + 'đ · ' + product.shop;
                pending.appendChild(link);
            }
        } catch (error) {
            pending.classList.add('error');
            pending.textContent = error.name === 'AbortError' ? 'Kết nối đang chậm. Bạn vui lòng thử lại sau.' : error.message;
        } finally {
            clearTimeout(timeout);
            send.disabled = false;
            messages.scrollTop = messages.scrollHeight;
        }
    });
})();
