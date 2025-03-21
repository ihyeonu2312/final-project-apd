package site.unoeyhi.apd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryName(String categoryName);

    Optional<Category> findById(Long id);  // ✅ 올바른 메서드

    Optional<Category> findByUrl(String url);  // ✅ 올바른 메서드

    



 }