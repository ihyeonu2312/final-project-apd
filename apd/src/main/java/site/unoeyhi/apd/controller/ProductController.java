package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.service.product.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    // 📌 상품 목록 조회 (GET 요청)
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        log.info("🛒 [ProductController] 전체 상품 조회 요청");

        List<ProductDto> products = productService.getAllProducts();

        return ResponseEntity.ok(products);
    }

    // 📦 상품 등록 (POST 요청)
    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody ProductDto productDto) {
        log.info("📦 [ProductController] 상품 등록 요청: {}", productDto);

        Product savedProduct = productService.saveProduct(productDto);

        if (savedProduct == null) {
            return ResponseEntity.internalServerError().body("상품 저장 실패");
        }

        return ResponseEntity.ok("상품 저장 성공! ID: " + savedProduct.getProductId());
    }
}
