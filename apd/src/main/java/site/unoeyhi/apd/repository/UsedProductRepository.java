package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.UsedProduct;

public interface UsedProductRepository extends JpaRepository<UsedProduct, Integer> {
    // 필요 시 커스텀 쿼리 작성 가능
}
