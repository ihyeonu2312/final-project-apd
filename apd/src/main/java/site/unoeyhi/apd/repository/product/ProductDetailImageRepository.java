package site.unoeyhi.apd.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.unoeyhi.apd.entity.ProductDetailImage;

import java.util.List;

@Repository
public interface ProductDetailImageRepository extends JpaRepository<ProductDetailImage, Long> {
    List<ProductDetailImage> findByProduct_ProductId(Long productId);
}
