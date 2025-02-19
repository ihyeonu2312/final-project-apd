package site.unoeyhi.apd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByUrl(String url);

    Optional<Category> findByCategoryName(String name);

    List<Category> findByCategoryId(Long categoryId);  // ✅ 올바른 메서드

    // List<Product> findByCategory(Long categoryId);


 }