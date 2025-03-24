package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.UsedProduct;

public interface UsedProductRepository extends JpaRepository<UsedProduct, Integer> {
}
