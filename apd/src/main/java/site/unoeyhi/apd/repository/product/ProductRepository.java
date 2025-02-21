package site.unoeyhi.apd.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);  // 중복 방지용 메서드
    List<Product> findByCategory(Category category); // ✅ 특정 카테고리에 속한 상품 조회
    List<Product> findByCategoryCategoryId(Long categoryId); //상품 카테고리별 상품 조회
    ;
    
}
