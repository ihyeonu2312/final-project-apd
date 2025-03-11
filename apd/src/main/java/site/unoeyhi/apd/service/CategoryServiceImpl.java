package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import site.unoeyhi.apd.dto.CategoryDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;

    // ✅ 모든 카테고리를 DTO로 변환하여 반환
    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDto) // ✅ 엔티티 -> DTO 변환
                .collect(Collectors.toList());
    }

    // ✅ 특정 카테고리를 DTO로 변환하여 반환
    @Transactional(readOnly = true)
    @Override
    public Optional<CategoryDto> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(this::convertToDto); // ✅ 엔티티 -> DTO 변환
    }

    // ✅ 카테고리 저장 (DTO 변환은 필요 없음)
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

    // ✅ 엔티티를 DTO로 변환하는 메서드
    private CategoryDto convertToDto(Category category) {
        return new CategoryDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getUrl()
        );
    }

}
