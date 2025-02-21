package site.unoeyhi.apd.service.product;

import org.springframework.stereotype.Service;
import site.unoeyhi.apd.dto.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
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


    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findByTitle(String title) {
        return productRepository.findByName(title);

        
    }
    
}
