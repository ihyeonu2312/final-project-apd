package site.unoeyhi.apd.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import site.unoeyhi.apd.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ✅ 특정 상품의 리뷰 목록 조회
    List<Review> findByProductProductId(Long productId);

    // ✅ 특정 회원이 작성한 리뷰 목록 조회
    List<Review> findByMemberId(Long memberId);

    // ✅ 특정 상품의 평균 평점 계산 (평점이 없을 경우 0 반환)
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.product.productId = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
}
