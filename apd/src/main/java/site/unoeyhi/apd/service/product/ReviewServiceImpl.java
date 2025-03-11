package site.unoeyhi.apd.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.dto.product.ReviewDto;
import site.unoeyhi.apd.entity.Review;
import site.unoeyhi.apd.repository.product.ReviewRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ReviewDto> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductProductId(productId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageRating(Long productId) {
        Double rating = reviewRepository.findAverageRatingByProductId(productId);
        return (rating != null) ? rating : 0.0; // ✅ NULL 방지
    }

    @Transactional
    @Override
    public ReviewDto addReview(ReviewDto reviewDto) {
        Review review = new Review();
        review.setProduct(productRepository.findById(reviewDto.getProductId())
                .orElseThrow(() -> new RuntimeException("상품 없음")));

        // ✅ `setMember()` 대신 `setMemberId()` 사용
        review.setMemberId(reviewDto.getMemberId());

        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setReviewImageUrl(reviewDto.getReviewImageUrl());

        // ✅ `LocalDateTime` → `String` 변환 후 저장
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        review.setCreatedAt(LocalDateTime.now().format(formatter));

        Review savedReview = reviewRepository.save(review);
        return convertToDto(savedReview);
    }

    private ReviewDto convertToDto(Review review) {
        return new ReviewDto(
            review.getReviewId(),               // ✅ 리뷰 ID
            review.getProduct().getProductId(), // ✅ 상품 ID
            review.getMemberId(),               // ✅ 작성한 회원 ID (Member 객체 아님)
            review.getRating(),                 // ✅ 평점
            review.getComment(),                // ✅ 리뷰 내용
            review.getReviewImageUrl(),         // ✅ 리뷰 이미지 URL
            review.getCreatedAt()               // ✅ 생성 날짜 (String)
        );
    }
}
