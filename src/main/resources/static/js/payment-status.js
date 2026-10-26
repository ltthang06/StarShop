(() => {
    const payment = document.getElementById('vnpay-payment');
    if (!payment || payment.dataset.state !== 'PENDING') return;
    let checking = false;
    const timer = setInterval(async () => {
        if (checking || document.hidden) return;
        checking = true;
        try {
            const response = await fetch(payment.dataset.statusUrl, { credentials: 'same-origin', cache: 'no-store' });
            if (response.ok && (response.headers.get('content-type') || '').includes('application/json')) {
                const result = await response.json();
                if (['PAID', 'FAILED', 'REVIEW_REQUIRED'].includes(result.state)) {
                    clearInterval(timer);
                    window.location.reload();
                }
            }
        } catch (error) {
        } finally {
            checking = false;
        }
    }, 15000);
    window.addEventListener('pagehide', () => clearInterval(timer), { once: true });
})();
