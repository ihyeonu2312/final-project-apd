package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.dto.product.ProductImageDto;
import site.unoeyhi.apd.service.product.ProductImageService;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images") // ✅ 상품 ID에 대한 이미지 API
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    // ✅ 특정 상품의 이미지 목록 조회
    @GetMapping
    public ResponseEntity<List<ProductImageDto>> getProductImages(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.getProductImages(productId));
    }
}
