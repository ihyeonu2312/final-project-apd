package site.unoeyhi.apd.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.Discount;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Discount findByProduct_ProductId(Long productId);
}
