package vn.iotstar.starshop.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.entity.Address;
import vn.iotstar.starshop.entity.Cart;
import vn.iotstar.starshop.entity.CartItem;
import vn.iotstar.starshop.entity.Order;
import vn.iotstar.starshop.entity.OrderDetail;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.ShippingProvider;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.enums.PaymentMethod;
import vn.iotstar.starshop.enums.PaymentStatus;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.repository.CustomerAddressRepository;
import vn.iotstar.starshop.repository.CustomerCartItemRepository;
import vn.iotstar.starshop.repository.CustomerCartRepository;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.repository.CustomerShippingProviderRepository;
import vn.iotstar.starshop.repository.OrderDetailRepository;
import vn.iotstar.starshop.repository.OrderRepository;
import vn.iotstar.starshop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerCheckoutService {

    private final UserRepository userRepository;
    private final CustomerCartRepository cartRepository;
    private final CustomerCartItemRepository cartItemRepository;
    private final CustomerCatalogRepository catalogRepository;
    private final CustomerAddressRepository addressRepository;
    private final CustomerShippingProviderRepository providerRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository detailRepository;

    @Transactional
    public List<Long> placeOrder(
            String email, Long addressId, Long providerId, String note) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tài khoản"));
        Address address = addressRepository.findByIdAndUserId(
                addressId, user.getId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy địa chỉ nhận hàng"));
        ShippingProvider provider = providerRepository.findByIdAndActiveTrue(
                providerId
        ).orElseThrow(() -> new IllegalArgumentException(
                "Đơn vị vận chuyển không còn hoạt động"));
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Giỏ hàng đang trống"));
        List<CartItem> items = cartItemRepository
                .findByCartIdOrderByIdAsc(cart.getId());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống");
        }

        String shippingAddress = formatAddress(address);
        String orderNote = note == null ? null : note.trim();
        if (orderNote != null && orderNote.length() > 500) {
            throw new IllegalArgumentException("Ghi chú quá dài");
        }

        List<PurchaseLine> purchaseLines = new ArrayList<>();
        items.stream()
                .sorted(Comparator.comparing(item ->
                        item.getProduct().getId()))
                .forEach(item -> purchaseLines.add(
                        validateAndLock(item)));

        Map<Long, List<PurchaseLine>> byShop = purchaseLines.stream()
                .collect(Collectors.groupingBy(
                        line -> line.product().getShop().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<Long> orderIds = new ArrayList<>();
        for (List<PurchaseLine> shopLines : byShop.values()) {
            BigDecimal subtotal = shopLines.stream()
                    .map(line -> line.price().multiply(
                            BigDecimal.valueOf(line.item().getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Order order = new Order();
            order.setUser(user);
            order.setShop(shopLines.get(0).product().getShop());
            order.setReceiverName(address.getRecipientName());
            order.setReceiverPhone(address.getPhone());
            order.setShippingAddress(shippingAddress);
            order.setNote(orderNote);
            order.setSubtotal(subtotal);
            order.setShippingProvider(provider);
            order.setShippingFee(provider.getBaseFee());
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setTotalAmount(subtotal.add(provider.getBaseFee()));
            order.setPaymentMethod(PaymentMethod.COD);
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setStatus(OrderStatus.NEW);
            order = orderRepository.save(order);

            for (PurchaseLine line : shopLines) {
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProduct(line.product());
                detail.setQuantity(line.item().getQuantity());
                detail.setUnitPrice(line.price());
                detail.setSubtotal(line.price().multiply(
                        BigDecimal.valueOf(line.item().getQuantity())));
                detailRepository.save(detail);

                Product product = line.product();
                product.setQuantity(product.getQuantity()
                        - line.item().getQuantity());
                product.setSoldCount(product.getSoldCount()
                        + line.item().getQuantity());
                if (product.getQuantity() == 0) {
                    product.setStatus(ProductStatus.OUT_OF_STOCK);
                }
            }
            orderIds.add(order.getId());
        }

        cartItemRepository.deleteAll(items);
        return orderIds;
    }

    private PurchaseLine validateAndLock(CartItem item) {
        Product product = catalogRepository.findByIdForUpdate(
                item.getProduct().getId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Sản phẩm không còn tồn tại"));
        if (product.getStatus() != ProductStatus.ACTIVE
                || product.getShop().getStatus() != ShopStatus.ACTIVE
                || !product.getCategory().isActive()
                || item.getQuantity() < 1
                || item.getQuantity() > product.getQuantity()) {
            throw new IllegalArgumentException(
                    "Sản phẩm " + product.getName()
                            + " không còn đủ hàng");
        }

        BigDecimal price = product.getDiscountPrice() == null
                ? product.getPrice() : product.getDiscountPrice();
        return new PurchaseLine(item, product, price);
    }

    private String formatAddress(Address address) {
        String formatted = String.join(", ", List.of(
                address.getAddressLine(),
                address.getWard() == null ? "" : address.getWard(),
                address.getDistrict() == null ? "" : address.getDistrict(),
                address.getProvince() == null ? "" : address.getProvince()
        ).stream().filter(part -> !part.isBlank()).toList());

        if (formatted.length() > 500) {
            throw new IllegalArgumentException("Địa chỉ nhận hàng quá dài");
        }
        return formatted;
    }

    private record PurchaseLine(
            CartItem item, Product product, BigDecimal price) {
    }
}
