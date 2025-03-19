package site.unoeyhi.apd.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.unoeyhi.apd.entity.ProductImage;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductProductId(Long productId); // ✅ 특정 상품의 이미지 리스트 가져오기

    /** ✅ 상품 상세 이미지 전체 조회 */
    @Query("SELECT p.productId AS productId, p.detailUrl AS detailUrl " +
           "FROM Product p " +
           "WHERE p.productId NOT IN (SELECT DISTINCT pdi.product.productId FROM ProductDetailImage pdi)")
    List<Map<String, Object>> findAllProductDetails();
}
