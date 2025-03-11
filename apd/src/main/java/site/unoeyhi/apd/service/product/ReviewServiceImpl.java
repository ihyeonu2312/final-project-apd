package site.unoeyhi.apd.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import site.unoeyhi.apd.dto.product.ReviewDto;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.Review;
import site.unoeyhi.apd.repository.product.ProductRepository;
import site.unoeyhi.apd.repository.product.ReviewRepository;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    // 🔹 특정 상품의 리뷰 리스트 가져오기
    @Override
    public List<ReviewDto> getReviewsByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProduct_ProductId(productId);
        return reviews.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // 🔹 특정 상품의 평균 별점 가져오기
    @Override
    public Double getAverageRatingByProductId(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    // 🔹 리뷰 추가
    @Transactional
    @Override
    public ReviewDto addReview(ReviewDto reviewDto) {
        Product product = productRepository.findById(reviewDto.getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다!"));

        Review review = new Review();
        review.setProduct(product);
        review.setMemberId(reviewDto.getMemberId());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setReviewImageUrl(reviewDto.getReviewImageUrl());
        review.setCreatedAt(reviewDto.getCreatedAt());

        Review savedReview = reviewRepository.save(review);
        return convertToDto(savedReview);
    }

    // 🔹 Review -> ReviewDto 변환
    private ReviewDto convertToDto(Review review) {
        return new ReviewDto(
                review.getReviewId(),
                review.getProduct().getProductId(),
                review.getMemberId(),
                review.getRating(),
                review.getComment(),
                review.getReviewImageUrl(),
                review.getCreatedAt()
        );
    }
}
