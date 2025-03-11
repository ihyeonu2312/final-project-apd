package site.unoeyhi.apd.service;

import site.unoeyhi.apd.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();  // ✅ 모든 카테고리 조회
    Optional<Category> getCategoryById(Long categoryId);  // ✅ 특정 카테고리 조회
    void saveCategories(List<Category> categories);  // ✅ 카테고리 저장
}
