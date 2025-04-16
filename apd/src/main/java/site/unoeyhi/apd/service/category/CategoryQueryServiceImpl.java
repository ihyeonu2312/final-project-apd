package site.unoeyhi.apd.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.dto.CategoryDto;
import site.unoeyhi.apd.dto.product.CategoryWithProductsDto;
import site.unoeyhi.apd.dto.product.ProductSummaryDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {
    
    private final CategoryRepository categoryRepository;

    // ✅ 모든 카테고리를 DTO로 변환하여 반환
    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ 특정 카테고리를 DTO로 변환하여 반환
    @Transactional(readOnly = true)
    @Override
    public Optional<CategoryDto> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(this::convertToDto);
    }

    // ✅ 특정 카테고리를 상품 포함 DTO로 변환하여 반환
    @Transactional(readOnly = true)
    @Override
    public Optional<CategoryWithProductsDto> getCategoryWithProducts(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(this::convertToDtoWithProducts);
    }

    // ✅ 기본 DTO 변환
    private CategoryDto convertToDto(Category category) {
        return new CategoryDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getUrl()
        );
    }

    // ✅ 상품 포함 DTO 변환
private CategoryWithProductsDto convertToDtoWithProducts(Category category) {
    List<ProductSummaryDto> productDtos = category.getProducts().stream()
            .map(product -> new ProductSummaryDto(
                    product.getProductId(),
                    product.getName(),
                    product.getImageUrl(),
                    product.getPrice()
            ))
            .toList();

    return new CategoryWithProductsDto(
            category.getCategoryId(),
            category.getCategoryName(),
            category.getUrl(),
            productDtos
    );
}
}
