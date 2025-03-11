package site.unoeyhi.apd.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;

    // ✅ 기존 `Category` 반환 유지
    @Transactional(readOnly = true)
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ✅ 기존 `Category` 반환 유지
    @Transactional(readOnly = true)
    @Override
    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    // ✅ 기존 저장 로직 유지
    @Transactional
    @Override
    public void saveCategories(List<Category> categories) {
        for (Category category : categories) {
            categoryRepository.findByCategoryName(category.getCategoryName())
                .ifPresentOrElse(
                    existingCategory -> System.out.println("이미 존재하는 카테고리: " + category.getCategoryName()),
                    () -> categoryRepository.save(category)
                );
        }
    }
}
