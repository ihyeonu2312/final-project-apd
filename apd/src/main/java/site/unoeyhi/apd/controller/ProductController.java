package site.unoeyhi.apd.controller;

import org.springframework.web.bind.annotation.*;

import site.unoeyhi.apd.dto.ProductDto;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.service.product.ProductService;

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

  // 특정 카테고리의 상품 조회 (ID 사용)
  @GetMapping("/category/{categoryId}")
  public List<Product> getProductsByCategory(@PathVariable("categoryId") Long categoryId) {
      return productService.getProductsByCategoryId(categoryId);
  }



}
