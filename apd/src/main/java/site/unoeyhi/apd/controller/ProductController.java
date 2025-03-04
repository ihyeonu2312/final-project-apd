package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody ProductDto productDto) {
        log.info("📦 [ProductController] 상품 등록 요청: {}", productDto);

        Product savedProduct = productService.saveProduct(productDto);  // 🚨 여기서 실행됨!

        if (savedProduct == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 저장 실패");
        }

        return ResponseEntity.ok("상품 저장 성공! ID: " + savedProduct.getProductId());
    }
}
