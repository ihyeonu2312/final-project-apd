package site.unoeyhi.apd.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.unoeyhi.apd.entity.ProductDetailImage;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductDetailImageRepository extends JpaRepository<ProductDetailImage, Long> {
    List<ProductDetailImage> findByProduct_ProductId(Long productId);

    /** ✅ 상품 상세 이미지 전체 조회 */
    @Query("SELECT p.productId AS productId, p.detailUrl AS detailUrl " +
           "FROM Product p " +
           "WHERE p.productId NOT IN (SELECT DISTINCT pdi.product.productId FROM ProductDetailImage pdi)")
    List<Map<String, Object>> findAllProductDetails();
}
