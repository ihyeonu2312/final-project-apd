package site.unoeyhi.apd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return categoryRepository.findById(categoryId);  
    }
    @Transactional
    public void saveCategories(List<Category> categories) {
        for (Category category : categories) {
            // 중복 체크 후 저장
            categoryRepository.findByCategoryName(category.getCategoryName())
                .ifPresentOrElse(
                    existingCategory -> System.out.println("이미 존재하는 카테고리: " + category.getCategoryName()),
                    () -> categoryRepository.save(category)
                );
        }
    }
}
