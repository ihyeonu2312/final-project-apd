package site.unoeyhi.apd.service.product;

import java.util.List;
import site.unoeyhi.apd.dto.product.ReviewDto;

public interface ReviewService {
    List<ReviewDto> getReviewsByProductId(Long productId);
    Double getAverageRating(Long productId); // ✅ 평균 평점 가져오기
    ReviewDto addReview(ReviewDto reviewDto);
}
