package site.unoeyhi.apd.controller;

import org.springframework.web.bind.annotation.*;

import site.unoeyhi.apd.dto.ProductDto;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public Product addProduct(@RequestBody ProductDto productDto) {
        return productService.saveProduct(productDto);
    }

    // 📌 모든 제품 조회
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
     // ✅ 특정 카테고리(예: 가전제품) 제품만 조회
     @GetMapping("/category/{categoryKey}")
     public List<Product> getProductsByCategory(@PathVariable("categoryKey") String categoryKey) {
         System.out.println("✅ API 요청 카테고리 키: " + categoryKey);
         List<Product> products = productService.getProductsByCategory(categoryKey);
         System.out.println("✅ 조회된 상품 수: " + products.size());
         return products;
     }
     
}
