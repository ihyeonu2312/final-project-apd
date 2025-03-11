package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.dto.CategoryDto;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.service.CategoryService;
import site.unoeyhi.apd.service.product.ProductService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    // ✅ 모든 카테고리 조회 (DTO 반환)
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // ✅ 특정 카테고리 조회 (DTO 반환)
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long categoryId) {
        Optional<CategoryDto> categoryDto = categoryService.getCategoryById(categoryId);
        return categoryDto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // ✅ 특정 카테고리에 속한 상품 조회
    @GetMapping("/{categoryId}/products")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductDto> products = productService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }
}
