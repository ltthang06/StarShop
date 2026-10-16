(() => {
    const menu = document.getElementById('manager-menu');
    const button = document.getElementById('manager-menu-toggle');
    if (!menu || !button) return;
    button.addEventListener('click', () => {
        const opened = menu.classList.toggle('is-open');
        button.setAttribute('aria-expanded', String(opened));
        button.textContent = opened ? 'Đóng menu' : 'Menu';
    });
    const current = location.pathname.replace(/\/$/, '');
    menu.querySelectorAll('a').forEach(link => {
        const target = new URL(link.href).pathname.replace(/\/$/, '');
        if (current === target || target.includes('/manager/') && current.startsWith(target + '/')) {
            link.classList.add('active');
            link.setAttribute('aria-current', 'page');
        }
    });
})();

