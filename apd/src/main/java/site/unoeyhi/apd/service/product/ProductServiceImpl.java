package site.unoeyhi.apd.service.product;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import site.unoeyhi.apd.dto.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductImage;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductImageRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService { 

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
    }

    @Override
    @Transactional
    public Product saveProduct(ProductDto productDto) {
        try {
            System.out.println("🚀 [saveProduct] 상품 저장 시작: " + productDto.getName());

            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("🚨 [saveProduct] 카테고리 ID가 존재하지 않습니다: " + productDto.getCategoryId()));

            System.out.println("✅ [saveProduct] 카테고리 찾음: " + category.getCategoryName());

            Product product = Product.builder()
                    .name(productDto.getName())
                    .price(productDto.getPrice())
                    .stockQuantity(productDto.getStockQuantity())
                    .category(category)
                    .imageUrl(productDto.getImageUrl())
                    .thumbnailImageUrl(productDto.getThumbnailImageUrl())
                    .detailUrl(productDto.getDetailUrl())
                    .build();

            Product savedProduct = productRepository.save(product);
            System.out.println("✅ [saveProduct] 저장된 상품 ID: " + savedProduct.getProductId());

            if (productDto.getAdditionalImages() != null && !productDto.getAdditionalImages().isEmpty()) {
                for (String imageUrl : productDto.getAdditionalImages()) {
                    ProductImage productImage = ProductImage.builder()
                            .product(savedProduct)
                            .imageUrl(imageUrl)
                            .build();
                    productImageRepository.save(productImage);
                }
                System.out.println("✅ [saveProduct] 추가 이미지 저장 완료");
            }

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
