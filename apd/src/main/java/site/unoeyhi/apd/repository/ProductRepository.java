package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);  // 중복 방지용 메서드
}
