package site.unoeyhi.apd.controller;

import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.dto.ProductDto;
import site.unoeyhi.apd.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products/bulk")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public Product addProduct(@RequestBody ProductDto productDto) {
        return productService.saveProduct(productDto);
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
}
