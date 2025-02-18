package site.unoeyhi.apd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
     // ✅ 특정 categoryId로 조회
    Optional<Category> findById(Long categoryId);

     // ✅ 특정 name으로 조회
    Optional<Category> findByName(String name);
 
     // ✅ 전체 카테고리 조회
    List<Category> findAll();

    Optional<Category> findByUrl(String url);


 }