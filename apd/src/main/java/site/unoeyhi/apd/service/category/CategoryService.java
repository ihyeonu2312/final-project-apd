package site.unoeyhi.apd.service.category;

import site.unoeyhi.apd.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();  // ✅ 기존 로직 유지
    Optional<Category> getCategoryById(Long categoryId);  // ✅ 기존 로직 유지
    void saveCategories(List<Category> categories);
}
