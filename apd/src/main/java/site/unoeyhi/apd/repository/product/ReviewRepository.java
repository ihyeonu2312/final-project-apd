package site.unoeyhi.apd.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import site.unoeyhi.apd.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 상품의 모든 리뷰 가져오기
    List<Review> findByProduct_ProductId(Long productId);

    // 특정 상품의 평균 별점 가져오기
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.product.productId = :productId")
    Double getAverageRatingByProductId(Long productId);
}
