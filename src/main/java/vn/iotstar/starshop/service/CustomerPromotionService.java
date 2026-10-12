package vn.iotstar.starshop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.CustomerCartLine;
import vn.iotstar.starshop.dto.CustomerCartSummary;
import vn.iotstar.starshop.dto.CustomerCheckoutQuote;
import vn.iotstar.starshop.dto.CustomerVoucherCard;
import vn.iotstar.starshop.entity.Promotion;
import vn.iotstar.starshop.entity.ShippingProvider;
import vn.iotstar.starshop.enums.PromotionScope;
import vn.iotstar.starshop.enums.PromotionType;
import vn.iotstar.starshop.repository.CustomerPromotionRepository;
import vn.iotstar.starshop.repository.CustomerShippingProviderRepository;

@Service
@RequiredArgsConstructor
public class CustomerPromotionService {

    private final CustomerCartService cartService;
    private final CustomerShippingProviderRepository providerRepository;
    private final CustomerPromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public CustomerCheckoutQuote quote(
            String email, Long providerId, String code) {

        CustomerCartSummary cart = cartService.summary(email);
        if (!cart.isReadyToCheckout()) {
            throw new IllegalArgumentException(
                    "Giỏ hàng chưa sẵn sàng đặt hàng");
        }
        ShippingProvider provider = providerRepository.findByIdAndActiveTrue(
                providerId
        ).orElseThrow(() -> new IllegalArgumentException(
                "Đơn vị vận chuyển không còn hoạt động"));

        Map<Long, BigDecimal> subtotals = shopSubtotals(cart);

        Map<Long, BigDecimal> discounts = calculate(
                findPromotion(code, false), subtotals,
                provider.getBaseFee());
        BigDecimal shippingFee = provider.getBaseFee().multiply(
                BigDecimal.valueOf(cart.getShopCount()));
        BigDecimal discount = discounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerCheckoutQuote(
                cart.getSubtotal(),
                shippingFee,
                discount,
                cart.getSubtotal().add(shippingFee).subtract(discount),
                normalize(code)
        );
    }

    @Transactional(readOnly = true)
    public List<CustomerVoucherCard> available(
            String email, Long providerId) {

        CustomerCartSummary cart = cartService.summary(email);
        if (!cart.isReadyToCheckout()) {
            return List.of();
        }
        ShippingProvider provider = providerRepository.findByIdAndActiveTrue(
                providerId
        ).orElseThrow(() -> new IllegalArgumentException(
                "Đơn vị vận chuyển không còn hoạt động"));

        Map<Long, BigDecimal> subtotals = shopSubtotals(cart);
        List<CustomerVoucherCard> vouchers = new ArrayList<>();
        for (Promotion promotion : promotionRepository.findAvailable(
                LocalDateTime.now())) {
            BigDecimal eligibleSubtotal = promotion.getScope()
                    == PromotionScope.SYSTEM
                    ? cart.getSubtotal()
                    : promotion.getShop() == null
                            ? null
                            : subtotals.get(promotion.getShop().getId());
            if (eligibleSubtotal == null
                    || promotion.getMinOrderAmount() != null
                    && eligibleSubtotal.compareTo(
                            promotion.getMinOrderAmount()) < 0) {
                continue;
            }

            BigDecimal discount = calculate(
                    promotion, subtotals, provider.getBaseFee())
                    .values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (discount.signum() > 0) {
                vouchers.add(new CustomerVoucherCard(
                        promotion.getCode(), promotion.getName(),
                        promotion.getDescription(), discount));
            }
        }
        return vouchers;
    }

    @Transactional
    public Map<Long, BigDecimal> apply(
            String code,
            Map<Long, BigDecimal> subtotals,
            BigDecimal shippingFeePerShop) {

        Promotion promotion = findPromotion(code, true);
        Map<Long, BigDecimal> discounts = calculate(
                promotion, subtotals, shippingFeePerShop);
        if (promotion != null) {
            promotion.setQuantity(promotion.getQuantity() - 1);
        }
        return discounts;
    }

    private Promotion findPromotion(String code, boolean locked) {
        String normalized = normalize(code);
        if (normalized == null) {
            return null;
        }

        Promotion promotion = (locked
                ? promotionRepository.findLockedByCodeIgnoreCase(normalized)
                : promotionRepository.findByCodeIgnoreCase(normalized))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mã giảm giá không tồn tại"));

        LocalDateTime now = LocalDateTime.now();
        if (!promotion.isActive()
                || promotion.getQuantity() < 1
                || now.isBefore(promotion.getStartAt())
                || now.isAfter(promotion.getEndAt())) {
            throw new IllegalArgumentException(
                    "Mã giảm giá đã hết hiệu lực");
        }
        return promotion;
    }

    private Map<Long, BigDecimal> calculate(
            Promotion promotion,
            Map<Long, BigDecimal> subtotals,
            BigDecimal shippingFeePerShop) {

        Map<Long, BigDecimal> discounts = new LinkedHashMap<>();
        subtotals.keySet().forEach(id -> discounts.put(
                id, BigDecimal.ZERO));
        if (promotion == null) {
            return discounts;
        }

        Map<Long, BigDecimal> eligible = new LinkedHashMap<>();
        if (promotion.getScope() == PromotionScope.SYSTEM) {
            eligible.putAll(subtotals);
        } else if (promotion.getScope() == PromotionScope.SHOP
                && promotion.getShop() != null
                && subtotals.containsKey(promotion.getShop().getId())) {
            Long shopId = promotion.getShop().getId();
            eligible.put(shopId, subtotals.get(shopId));
        } else {
            throw new IllegalArgumentException(
                    "Mã giảm giá không áp dụng cho giỏ hàng này");
        }

        BigDecimal eligibleSubtotal = eligible.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (promotion.getMinOrderAmount() != null
                && eligibleSubtotal.compareTo(
                        promotion.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException(
                    "Đơn hàng chưa đạt giá trị tối thiểu của mã");
        }

        BigDecimal eligibleShipping = shippingFeePerShop.multiply(
                BigDecimal.valueOf(eligible.size()));
        BigDecimal discount = switch (promotion.getType()) {
            case PERCENT -> eligibleSubtotal
                    .multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2,
                            RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> promotion.getDiscountValue();
            case FREE_SHIPPING -> eligibleShipping;
        };
        if (promotion.getMaxDiscountAmount() != null) {
            discount = discount.min(
                    promotion.getMaxDiscountAmount());
        }
        BigDecimal maximum = promotion.getType()
                == PromotionType.FREE_SHIPPING
                ? eligibleShipping : eligibleSubtotal;
        discount = discount.min(maximum).max(BigDecimal.ZERO);

        Map<Long, BigDecimal> weights = new LinkedHashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : eligible.entrySet()) {
            weights.put(entry.getKey(),
                    promotion.getType() == PromotionType.FREE_SHIPPING
                            ? shippingFeePerShop : entry.getValue());
        }
        BigDecimal weightTotal = weights.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (weightTotal.signum() == 0) {
            return discounts;
        }

        BigDecimal remaining = discount;
        int index = 0;
        for (Map.Entry<Long, BigDecimal> entry : weights.entrySet()) {
            index++;
            BigDecimal share = index == weights.size()
                    ? remaining
                    : discount.multiply(entry.getValue())
                            .divide(weightTotal, 2, RoundingMode.DOWN);
            discounts.put(entry.getKey(), share);
            remaining = remaining.subtract(share);
        }
        return discounts;
    }

    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim();
    }

    private Map<Long, BigDecimal> shopSubtotals(
            CustomerCartSummary cart) {
        Map<Long, BigDecimal> subtotals = new LinkedHashMap<>();
        for (CustomerCartLine line : cart.getLines()) {
            subtotals.merge(line.getShopId(), line.getSubtotal(),
                    BigDecimal::add);
        }
        return subtotals;
    }
}
