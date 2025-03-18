package site.unoeyhi.apd.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);  // 중복 방지용 메서드
    List<Product> findByCategory(Category category); // ✅ 특정 카테고리에 속한 상품 조회
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.categoryId = :categoryId")
    List<Product> findByCategoryCategoryId(@Param("categoryId") Long categoryId);

    Optional<Product> findById(Long productId);
}
