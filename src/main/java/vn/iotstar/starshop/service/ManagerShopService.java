package vn.iotstar.starshop.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;

import vn.iotstar.starshop.entity.Commission;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.enums.ShopStatus;

public interface ManagerShopService {

    Page<Shop> search(String keyword, ShopStatus status, int page);

    Shop findById(Long id);

    long countPending();

    void approve(Long id);

    void reject(Long id);

    void block(Long id);

    void reopen(Long id);

    Optional<Commission> currentCommission(Long shopId);

    void setCommission(Long shopId, BigDecimal ratePercent);
}
