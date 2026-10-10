package vn.iotstar.starshop.service;

import org.springframework.data.domain.Page;

import vn.iotstar.starshop.dto.PromotionRequest;
import vn.iotstar.starshop.entity.Promotion;

public interface PromotionService {

    Page<Promotion> searchVendorPromotions(
            Long shopId,
            Long ownerId,
            String keyword,
            Boolean active,
            int page,
            int size
    );

    Promotion getPromotionByOwner(
            Long promotionId,
            Long shopId,
            Long ownerId
    );

    Promotion createPromotion(
            Long shopId,
            Long ownerId,
            PromotionRequest request
    );

    Promotion updatePromotion(
            Long promotionId,
            Long shopId,
            Long ownerId,
            PromotionRequest request
    );

    void setActive(
            Long promotionId,
            Long shopId,
            Long ownerId,
            boolean active
    );
}