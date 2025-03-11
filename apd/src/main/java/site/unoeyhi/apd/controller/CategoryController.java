package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.dto.CategoryDto;
import site.unoeyhi.apd.dto.product.CategoryWithProductsDto;
import site.unoeyhi.apd.service.category.CategoryQueryService;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryQueryService categoryQueryService;  // ✅ DTO 전용 서비스 사용

    // ✅ 모든 카테고리 조회 (상품 목록 없이)
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryQueryService.getAllCategories());
    }

    // ✅ 특정 카테고리 조회 (상품 목록 없이)
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long categoryId) {
        return categoryQueryService.getCategoryById(categoryId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ 특정 카테고리 조회 (상품 포함)
    @GetMapping("/{categoryId}/products")
    public ResponseEntity<CategoryWithProductsDto> getCategoryWithProducts(@PathVariable Long categoryId) {
        return categoryQueryService.getCategoryWithProducts(categoryId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
