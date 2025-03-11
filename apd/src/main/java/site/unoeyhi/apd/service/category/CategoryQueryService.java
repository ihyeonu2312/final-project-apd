package site.unoeyhi.apd.service.category;

import site.unoeyhi.apd.dto.CategoryDto;
import site.unoeyhi.apd.dto.product.CategoryWithProductsDto;
import java.util.List;
import java.util.Optional;

public interface CategoryQueryService {
    List<CategoryDto> getAllCategories();  // ✅ DTO 반환 전용
    Optional<CategoryDto> getCategoryById(Long categoryId);
    Optional<CategoryWithProductsDto> getCategoryWithProducts(Long categoryId);
}
