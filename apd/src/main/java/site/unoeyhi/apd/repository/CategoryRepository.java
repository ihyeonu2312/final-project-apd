package site.unoeyhi.apd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name); // ✅ 카테고리 이름으로 조회
}