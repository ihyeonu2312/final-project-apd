package site.unoeyhi.apd.service.product;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
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
    @Transactional
    public Product saveProduct(ProductDto productDto) {
        try {
            System.out.println("🚀 [saveProduct] 상품 저장 시작: " + productDto.getName());
            System.out.println("🚀 [saveProduct] categoryId: " + productDto.getCategoryId());
    
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("🚨 [saveProduct] 카테고리 ID가 존재하지 않습니다: " + productDto.getCategoryId()));
    
            System.out.println("✅ [saveProduct] 카테고리 찾음: " + category.getCategoryName());
    
            Product product = Product.builder()
                    .adminId(productDto.getAdminId())
                    .name(productDto.getName())
                    .description(productDto.getDescription())
                    .price(productDto.getPrice())
                    .stockQuantity(productDto.getStockQuantity())
                    .imageUrl(productDto.getImageUrl())
                    .category(category)
                    .build();
    
            Product savedProduct = productRepository.save(product);
            System.out.println("✅ [saveProduct] 저장된 상품 ID: " + savedProduct.getProductId());
            System.out.println("✅ [saveProduct] 저장된 상품의 category_id: " + savedProduct.getCategory().getCategoryId());
    
            return savedProduct;
        } catch (Exception e) {
            System.out.println("🚨 [saveProduct] 상품 저장 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    




    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findByTitle(String title) {
        return productRepository.findByName(title);
    }
    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
    }


    
}
