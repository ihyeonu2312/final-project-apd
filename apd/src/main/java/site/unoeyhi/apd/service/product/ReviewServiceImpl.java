package site.unoeyhi.apd.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.dto.product.ReviewDto;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.Review;
import site.unoeyhi.apd.repository.product.ReviewRepository;
import site.unoeyhi.apd.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

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
        String nickname = memberRepository.findById(review.getMemberId())
                            .map(Member::getNickname)
                            .orElse("탈퇴한 회원");
    
        return new ReviewDto(
            review.getReviewId(),
            review.getProduct().getProductId(),
            review.getMemberId(),
            nickname, // ✅ 추가된 필드
            review.getRating(),
            review.getComment(),
            review.getReviewImageUrl(),
            review.getCreatedAt()
        );
    }
    
}
