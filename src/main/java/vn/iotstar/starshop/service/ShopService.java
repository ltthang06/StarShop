package vn.iotstar.starshop.service;

import java.util.List;

import vn.iotstar.starshop.dto.ShopRequest;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;

public interface ShopService {

    Shop registerShop(User owner, ShopRequest request);

    List<Shop> getShopsByOwner(Long ownerId);

    Shop getShopByOwner(Long shopId, Long ownerId);

    Shop updateShop(Long shopId, Long ownerId, ShopRequest request);
}