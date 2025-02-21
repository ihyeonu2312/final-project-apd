package site.unoeyhi.apd.service;

import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ✅ 모든 카테고리를 조회하는 메서드 추가
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findByCategoryId(categoryId);  
    }
}
