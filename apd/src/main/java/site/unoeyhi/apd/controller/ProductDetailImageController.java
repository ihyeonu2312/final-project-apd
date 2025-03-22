package site.unoeyhi.apd.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.ProductDetailImage;
import site.unoeyhi.apd.service.product.ProductDetailImageService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductDetailImageController {

    private final ProductDetailImageService productDetailImageService;

    @GetMapping("/{productId}/detail-images")
    public ResponseEntity<List<String>> getProductDetailImages(@PathVariable Long productId) {
        List<ProductDetailImage> images = productDetailImageService.getDetailImagesByProductId(productId);
        List<String> imageUrls = images.stream()
                .map(ProductDetailImage::getImageUrl)
                .toList();
        return ResponseEntity.ok(imageUrls);
    }
}
