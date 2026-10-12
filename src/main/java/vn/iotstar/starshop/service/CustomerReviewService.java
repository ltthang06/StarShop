package vn.iotstar.starshop.service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.CustomerReviewCard;
import vn.iotstar.starshop.dto.CustomerReviewTarget;
import vn.iotstar.starshop.entity.OrderDetail;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.Review;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.repository.CustomerReviewRepository;
import vn.iotstar.starshop.repository.OrderDetailRepository;
import vn.iotstar.starshop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerReviewService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserRepository userRepository;
    private final OrderDetailRepository detailRepository;
    private final CustomerReviewRepository reviewRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public List<CustomerReviewCard> reviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(
                productId, PageRequest.of(0, 20))
                .stream()
                .map(review -> new CustomerReviewCard(
                        review.getUser().getFullName(),
                        review.getRating(),
                        review.getContent(),
                        review.getImageUrl(),
                        review.getVideoUrl(),
                        review.getCreatedAt().format(DATE_FORMAT)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerReviewTarget target(
            String email, Long orderId, Long detailId) {

        OrderDetail detail = reviewableDetail(email, orderId, detailId);
        return new CustomerReviewTarget(
                orderId, detailId, detail.getProduct().getName());
    }

    @Transactional
    public void submit(
            String email,
            Long orderId,
            Long detailId,
            int rating,
            String content,
            MultipartFile image,
            MultipartFile video) throws IOException {

        OrderDetail detail = reviewableDetail(email, orderId, detailId);
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5");
        }
        String text = content == null ? "" : content.trim();
        if (text.length() < 50 || text.length() > 2000) {
            throw new IllegalArgumentException(
                    "Nội dung đánh giá phải từ 50 đến 2000 ký tự");
        }

        validateFile(image, Set.of(
                "image/jpeg", "image/png", "image/webp"), "Ảnh");
        validateFile(video, Set.of(
                "video/mp4", "video/webm"), "Video");

        String imageUrl = null;
        String videoUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadReviewImage(image).getUrl();
        }
        if (video != null && !video.isEmpty()) {
            videoUrl = cloudinaryService.uploadReviewVideo(video).getUrl();
        }

        Review review = new Review();
        review.setUser(detail.getOrder().getUser());
        review.setProduct(detail.getProduct());
        review.setOrderDetail(detail);
        review.setRating(rating);
        review.setContent(text);
        review.setImageUrl(imageUrl);
        review.setVideoUrl(videoUrl);
        reviewRepository.saveAndFlush(review);

        Product product = detail.getProduct();
        product.setRating(reviewRepository.averageRating(
                product.getId()));
    }

    private OrderDetail reviewableDetail(
            String email, Long orderId, Long detailId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tài khoản"));
        OrderDetail detail = detailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy sản phẩm trong đơn"));

        if (!detail.getOrder().getId().equals(orderId)
                || !detail.getOrder().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Không tìm thấy sản phẩm trong đơn");
        }
        if (detail.getOrder().getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "Chỉ có thể đánh giá sản phẩm đã giao");
        }
        if (reviewRepository.existsByOrderDetailId(detailId)) {
            throw new IllegalArgumentException(
                    "Sản phẩm này đã được đánh giá");
        }
        return detail;
    }

    private void validateFile(
            MultipartFile file, Set<String> allowedTypes, String label) {

        if (file == null || file.isEmpty()) {
            return;
        }
        if (file.getSize() > 10 * 1024 * 1024
                || !allowedTypes.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    label + " phải đúng định dạng và không quá 10 MB");
        }
    }
}
