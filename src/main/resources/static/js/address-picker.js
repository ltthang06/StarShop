(() => {
    const form = document.querySelector('[data-address-picker]');
    if (!form) return;

    const provinceSelect = form.querySelector('#province');
    const wardSelect = form.querySelector('#ward');
    const status = form.querySelector('#addressPickerStatus');
    const savedProvince = form.dataset.province || '';
    const savedWard = form.dataset.ward || '';
    let provinces = [];

    const normalize = (value, prefixes) => {
        let normalized = String(value || '').trim().toLocaleLowerCase('vi');
        normalized = normalized.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd');
        for (const prefix of prefixes) {
            const expression = new RegExp(`^(?:${prefix})\\.?\\s+`, 'i');
            normalized = normalized.replace(expression, '');
        }
        return normalized.replace(/[^a-z0-9]+/g, ' ').trim();
    };

    const setPlaceholder = (select, label) => {
        select.replaceChildren(new Option(label, ''));
    };

    const loadWards = (provinceCode, selectedWard = '') => {
        const province = provinces.find(item => item.code === provinceCode || item.name === provinceCode);
        setPlaceholder(wardSelect, province ? 'Chọn phường/xã' : 'Chọn tỉnh/thành phố trước');
        wardSelect.disabled = !province;
        if (!province) return;

        for (const commune of province.communes) {
            wardSelect.add(new Option(commune.name, commune.name));
        }
        const selected = province.communes.find(item =>
            normalize(item.name, ['phuong', 'xa', 'thi tran', 'dac khu']) ===
            normalize(selectedWard, ['phuong', 'xa', 'thi tran', 'dac khu'])
        );
        if (selected) wardSelect.value = selected.name;
        else if (selectedWard) status.textContent = 'Phường/xã đã lưu thuộc danh mục cũ. Vui lòng chọn lại theo danh mục hiện hành.';
    };

    provinceSelect.addEventListener('change', () => {
        status.textContent = '';
        loadWards(provinceSelect.value);
    });

    fetch(form.dataset.source, { headers: { Accept: 'application/json' } })
        .then(response => {
            if (!response.ok) throw new Error('Không tải được danh sách địa chỉ.');
            return response.json();
        })
        .then(data => {
            provinces = data.provinces || [];
            setPlaceholder(provinceSelect, 'Chọn tỉnh/thành phố');
            for (const province of provinces) {
                provinceSelect.add(new Option(province.name, province.name));
            }
            provinceSelect.disabled = false;

            const selectedProvince = provinces.find(item => item.code === savedProvince ||
                normalize(item.name, ['thanh pho', 'tp', 'tinh']) ===
                normalize(savedProvince, ['thanh pho', 'tp', 'tinh'])
            );
            if (selectedProvince) {
                provinceSelect.value = selectedProvince.name;
                loadWards(selectedProvince.code, savedWard);
                if (!savedWard) status.textContent = 'Địa chỉ đang thiếu phường/xã. Vui lòng chọn bổ sung.';
            } else {
                setPlaceholder(wardSelect, 'Chọn tỉnh/thành phố trước');
                wardSelect.disabled = true;
            }
        })
        .catch(() => {
            setPlaceholder(provinceSelect, 'Không tải được danh sách');
            setPlaceholder(wardSelect, 'Thử tải lại trang');
            status.textContent = 'Không tải được danh sách địa chỉ. Hãy tải lại trang để thử lại.';
        });
})();
