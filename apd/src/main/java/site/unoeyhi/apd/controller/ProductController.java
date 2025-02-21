// package site.unoeyhi.apd.controller;

// import org.springframework.web.bind.annotation.*;

// import site.unoeyhi.apd.dto.ProductDto;
// import site.unoeyhi.apd.entity.Product;
// import site.unoeyhi.apd.service.product.ProductService;

// import java.util.List;

// @RestController
// @RequestMapping("/api/products")
// public class ProductController {
//     private final ProductService productService;

//     public ProductController(ProductService productService) {
//         this.productService = productService;
//     }

//     @PostMapping
//     public Product addProduct(@RequestBody ProductDto productDto) {
//         return productService.saveProduct(productDto);
//     }

//     // 📌 모든 제품 조회
//     @GetMapping
//     public List<Product> getAllProducts() {
//         return productService.getAllProducts();
//     }
//      // ✅ 특정 카테고리(예: 가전제품) 제품만 조회
//      @GetMapping("/category/{coupangCategoryKey}")
//      public List<Product> getProductsByCategory(@PathVariable("coupangCategoryKey") String coupangCategoryKey) {
//          System.out.println("✅ API 요청 카테고리 키: " + coupangCategoryKey);
//          List<Product> products = productService.getProductsByCategory(coupangCategoryKey);
//          System.out.println("✅ 조회된 상품 수: " + products.size());
//          return products;
//      }
     
// }
