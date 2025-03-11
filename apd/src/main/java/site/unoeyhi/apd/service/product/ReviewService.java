package site.unoeyhi.apd.service.product;

import java.util.List;
import site.unoeyhi.apd.dto.product.ReviewDto;

public interface ReviewService {
    List<ReviewDto> getReviewsByProductId(Long productId);
    Double getAverageRatingByProductId(Long productId);
    ReviewDto addReview(ReviewDto reviewDto);
}
