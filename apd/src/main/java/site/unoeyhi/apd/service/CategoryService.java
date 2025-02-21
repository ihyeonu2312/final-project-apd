package site.unoeyhi.apd.service;

import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // public Category saveCategory(Category category) {
    //     // 중복 확인: URL이 존재하면 업데이트, 없으면 새로 추가
    //     Optional<Category> existingCategory = categoryRepository.findByUrl(category.getCategoryUrl());
    //     if (existingCategory.isPresent()) {
    //         Category updatedCategory = existingCategory.get();
    //         updatedCategory.setCategoryName(category.getCategoryName());
    //         updatedCategory.setCategoryName(category.getCategoryName());
    //         return categoryRepository.save(updatedCategory);
    //     } else {
    //         return categoryRepository.save(category);
    //     }
    // }


    // ✅ 모든 카테고리를 조회하는 메서드 추가
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    public List<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findByCategoryId(categoryId);  
    }
}
