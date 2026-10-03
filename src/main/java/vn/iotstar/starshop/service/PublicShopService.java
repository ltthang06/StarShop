package vn.iotstar.starshop.service;

import vn.iotstar.starshop.dto.PublicShopData;

public interface PublicShopService {

    PublicShopData getPublicShop(
            Long shopId,
            String keyword,
            Long categoryId,
            int page,
            int size
    );
}