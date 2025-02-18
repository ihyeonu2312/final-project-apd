package site.unoeyhi.apd.service;

import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.*;
import site.unoeyhi.apd.entity.dto.ProductDto;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.ProductRepository;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product saveProduct(ProductDto productDto) {
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 ID가 존재하지 않습니다."));

        Product product = Product.builder()
                .adminId(productDto.getAdminId())
                .name(productDto.getName())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .stockQuantity(productDto.getStockQuantity())
                .category(category)
                .imageUrl(productDto.getImageUrl())
                .build();

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
