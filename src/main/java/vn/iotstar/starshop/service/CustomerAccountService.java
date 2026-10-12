package vn.iotstar.starshop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.entity.Address;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.repository.CustomerAccountRepository;
import vn.iotstar.starshop.repository.CustomerAddressRepository;
import vn.iotstar.starshop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerAccountService {

    private final UserRepository userRepository;
    private final CustomerAccountRepository accountRepository;
    private final CustomerAddressRepository addressRepository;

    @Transactional(readOnly = true)
    public User profile(String email) {
        return currentUser(email);
    }

    @Transactional
    public void updateProfile(
            String email,
            String fullName,
            String phone) {

        User user = currentUser(email);
        if (fullName == null || fullName.isBlank()
                || fullName.trim().length() > 100) {
            throw new IllegalArgumentException("Họ tên không hợp lệ");
        }

        String normalizedPhone = phone == null ? "" : phone.trim();
        if (!normalizedPhone.isEmpty()
                && !normalizedPhone.matches("[0-9+ ]{9,20}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        }
        if (!normalizedPhone.isEmpty()
                && accountRepository.existsByPhoneAndIdNot(
                        normalizedPhone, user.getId())) {
            throw new IllegalArgumentException(
                    "Số điện thoại đã được sử dụng");
        }

        user.setFullName(fullName.trim());
        user.setPhone(normalizedPhone.isEmpty() ? null : normalizedPhone);
    }

    @Transactional(readOnly = true)
    public List<Address> addresses(String email) {
        User user = currentUser(email);
        return addressRepository
                .findByUserIdOrderByDefaultAddressDescIdAsc(user.getId());
    }

    @Transactional(readOnly = true)
    public Address address(String email, Long addressId) {
        User user = currentUser(email);
        return addressRepository.findByIdAndUserId(
                addressId, user.getId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy địa chỉ"));
    }

    @Transactional
    public void saveAddress(
            String email,
            Long addressId,
            String recipientName,
            String phone,
            String addressLine,
            String ward,
            String district,
            String province,
            boolean makeDefault) {

        User user = currentUser(email);
        validateAddress(
                recipientName,
                phone,
                addressLine,
                ward,
                district,
                province
        );

        List<Address> existing = addressRepository
                .findByUserIdOrderByDefaultAddressDescIdAsc(user.getId());

        Address address = addressId == null
                ? new Address()
                : addressRepository.findByIdAndUserId(
                        addressId, user.getId()
                ).orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy địa chỉ"));

        if (makeDefault || existing.isEmpty()) {
            existing.forEach(item -> item.setDefaultAddress(false));
            address.setDefaultAddress(true);
        }

        address.setUser(user);
        address.setRecipientName(recipientName.trim());
        address.setPhone(phone.trim());
        address.setAddressLine(addressLine.trim());
        address.setWard(trimOrNull(ward));
        address.setDistrict(trimOrNull(district));
        address.setProvince(trimOrNull(province));
        addressRepository.save(address);
    }

    @Transactional
    public void setDefault(String email, Long addressId) {
        User user = currentUser(email);
        List<Address> addresses = addressRepository
                .findByUserIdOrderByDefaultAddressDescIdAsc(user.getId());

        Address selected = addresses.stream()
                .filter(address -> address.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy địa chỉ"));

        addresses.forEach(address -> address.setDefaultAddress(false));
        selected.setDefaultAddress(true);
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = currentUser(email);
        Address address = addressRepository.findByIdAndUserId(
                addressId, user.getId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy địa chỉ"));

        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);
        addressRepository.flush();

        if (wasDefault) {
            List<Address> remaining = addressRepository
                    .findByUserIdOrderByDefaultAddressDescIdAsc(
                            user.getId());
            if (!remaining.isEmpty()) {
                remaining.get(0).setDefaultAddress(true);
            }
        }
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tài khoản"));
    }

    private void validateAddress(
            String recipientName,
            String phone,
            String addressLine,
            String ward,
            String district,
            String province) {

        if (recipientName == null || recipientName.isBlank()
                || recipientName.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Tên người nhận không hợp lệ");
        }
        if (phone == null
                || !phone.trim().matches("[0-9+ ]{9,20}")) {
            throw new IllegalArgumentException(
                    "Số điện thoại người nhận không hợp lệ");
        }
        if (addressLine == null || addressLine.isBlank()
                || addressLine.trim().length() > 255) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập địa chỉ giao hàng");
        }
        if (tooLong(ward) || tooLong(district) || tooLong(province)) {
            throw new IllegalArgumentException(
                    "Phường, quận hoặc tỉnh quá dài");
        }
    }

    private boolean tooLong(String value) {
        return value != null && value.trim().length() > 100;
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
