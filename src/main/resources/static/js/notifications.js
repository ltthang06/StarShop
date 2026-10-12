(() => {
    const script = document.currentScript;
    const context = new URL(script.src).pathname.replace(/\/js\/notifications\.js$/, '');
    const badge = document.getElementById('notification-count');
    let timer;
    async function refresh() {
        try {
            const response = await fetch(context + '/notifications/unread', {headers: {'Accept': 'application/json'}});
            if (response.ok && response.headers.get('content-type')?.includes('application/json')) {
                const result = await response.json();
                if (badge) badge.textContent = result.count || '';
            }
        } catch (_) {}
    }
    function connect() {
        const socket = new WebSocket((location.protocol === 'https:' ? 'wss://' : 'ws://') + location.host + context + '/ws/notifications');
        socket.onmessage = () => {
            refresh();
            const notice = document.getElementById('live-notice');
            if (notice) notice.hidden = false;
        };
        socket.onclose = event => {if (event.code !== 1008) timer = setTimeout(connect, 10000);};
        window.addEventListener('pagehide', () => {clearTimeout(timer);socket.onclose=null;socket.close();}, {once:true});
    }
    refresh();
    connect();
})();
