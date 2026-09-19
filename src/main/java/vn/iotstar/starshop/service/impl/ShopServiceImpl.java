package vn.iotstar.starshop.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.ShopRequest;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.repository.ShopRepository;
import vn.iotstar.starshop.service.ShopService;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    @Override
    public Shop registerShop(User owner, ShopRequest request) {

        if (owner == null || owner.getId() == null) {
            throw new IllegalArgumentException("Owner không hợp lệ");
        }

        Shop shop = new Shop();

        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());
        shop.setAddress(request.getAddress());
        shop.setLogo(request.getLogo());
        shop.setBanner(request.getBanner());
        shop.setStatus(ShopStatus.PENDING);
        shop.setOwner(owner);

        return shopRepository.save(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shop> getShopsByOwner(Long ownerId) {

        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID không hợp lệ");
        }

        return shopRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Shop getShopByOwner(Long shopId, Long ownerId) {

        return shopRepository
                .findByIdAndOwnerId(shopId, ownerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy shop"));
    }

    @Override
    public Shop updateShop(
            Long shopId,
            Long ownerId,
            ShopRequest request) {

        Shop shop = shopRepository
                .findByIdAndOwnerId(shopId, ownerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy shop"));

        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());
        shop.setAddress(request.getAddress());
        shop.setLogo(request.getLogo());
        shop.setBanner(request.getBanner());

        return shopRepository.save(shop);
    }
}