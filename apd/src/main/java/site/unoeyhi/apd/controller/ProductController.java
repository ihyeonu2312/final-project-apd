package site.unoeyhi.apd.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.service.product.ProductService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ✅ 모든 상품 조회
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ✅ 특정 카테고리의 상품 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

   // ✅ 특정 상품 조회 추가
@GetMapping("/{productId}")
public ResponseEntity<ProductDto> getProductById(@PathVariable Long productId) {
    return productService.getProductById(productId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}

}

