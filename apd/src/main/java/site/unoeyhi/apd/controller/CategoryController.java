// package site.unoeyhi.apd.controller;

// import java.util.List;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import site.unoeyhi.apd.entity.Category;
// import site.unoeyhi.apd.entity.Product;
// import site.unoeyhi.apd.service.CategoryService;
// import site.unoeyhi.apd.service.product.ProductService;

// @RestController
// @RequestMapping("/api/categories")
// public class CategoryController {
//     private final CategoryService categoryService;
//     private final ProductService productService;

//     public CategoryController(CategoryService categoryService , ProductService productService) {
//         this.categoryService = categoryService;
//         this.productService = productService;
//     }

//     // @PostMapping
//     // public Category addCategory(@RequestBody Category category) {
//     //     return categoryService.saveCategory(category);
//     // }

//     // CategoryController.java 예시
//     @GetMapping("/api/products/category/{CategoryName}")
//     public ResponseEntity<?> getProductsByCategory(@PathVariable String coupangCategoryKey) {
//         try {
//             List<Product> products = productService.getProductsByCategory(coupangCategoryKey);
//             return ResponseEntity.ok(products);
//         } catch (Exception e) {
//             e.printStackTrace(); // ✅ 서버 로그에 자세한 에러 출력
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류 발생");
//         }
//     }

// }
