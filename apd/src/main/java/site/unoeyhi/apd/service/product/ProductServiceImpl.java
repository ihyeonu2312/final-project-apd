package site.unoeyhi.apd.service.product;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import site.unoeyhi.apd.dto.product.OptionDto;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Option;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductImage;
import site.unoeyhi.apd.entity.ProductOption;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductImageRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;
import site.unoeyhi.apd.repository.product.OptionRepository;
import site.unoeyhi.apd.repository.product.ProductOptionRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional // ✅ 클래스 전체에 트랜잭션 적용
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final OptionRepository optionRepository;
    private final ProductOptionRepository productOptionRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                              ProductImageRepository productImageRepository, OptionRepository optionRepository,
                              ProductOptionRepository productOptionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.optionRepository = optionRepository;
        this.productOptionRepository = productOptionRepository;
    }

    @Override
    @Transactional
    public Product saveProduct(ProductDto productDto) {
        System.out.println("🚀 [saveProduct] 상품 저장 시작: " + productDto.getName());

        try {
            // ✅ 카테고리 찾기
            Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 카테고리 ID: " + productDto.getCategoryId()));

            // ✅ 상품 저장
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

            // ✅ 추가 이미지 저장 (비어있을 경우 저장하지 않음)
            if (productDto.getAdditionalImages() != null && !productDto.getAdditionalImages().isEmpty()) {
                for (String imageUrl : productDto.getAdditionalImages()) {
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) { // ✅ 빈 이미지 필터링
                        ProductImage productImage = ProductImage.builder()
                                .product(savedProduct)
                                .imageUrl(imageUrl)
                                .build();
                        productImageRepository.save(productImage);
                        System.out.println("🖼️ [saveProduct] 추가 이미지 저장 완료: " + imageUrl);
                    }
                }
            } else {
                System.out.println("⚠️ [saveProduct] 추가 이미지가 없습니다!");
            }

            // ✅ 옵션 저장 (비어있을 경우 저장하지 않음)
            if (productDto.getOptions() != null && !productDto.getOptions().isEmpty()) {
                for (OptionDto optionDto : productDto.getOptions()) {
                    if (optionDto.getOptionValue() == null || optionDto.getOptionValue().trim().isEmpty()) {
                        System.out.println("⚠️ [saveProduct] 옵션 값이 비어있어 저장하지 않음: " + optionDto);
                        continue;
                    }

                    Optional<Option> existingOption = optionRepository.findByOptionValueTypeAndOptionValue(
                        optionDto.getOptionValueType(), optionDto.getOptionValue());

                    Option option = existingOption.orElseGet(() -> {
                        Option newOption = Option.builder()
                                .optionValueType(optionDto.getOptionValueType())
                                .optionValue(optionDto.getOptionValue())
                                .build();
                        return optionRepository.save(newOption);
                    });

                    ProductOption productOption = ProductOption.builder()
                            .product(savedProduct)
                            .option(option)
                            .build();
                    productOptionRepository.save(productOption);
                    System.out.println("✅ [saveProduct] 옵션 저장 완료: " + optionDto.getOptionValueType() + " - " + optionDto.getOptionValue());
                }
            } else {
                System.out.println("⚠️ [saveProduct] 옵션 데이터가 없습니다!");
            }

            System.out.println("✅ [saveProduct] 상품, 이미지, 옵션 최종 저장 완료");
            return savedProduct;

        } catch (Exception e) {
            System.out.println("🚨 [saveProduct] 상품 저장 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("상품 저장 중 오류 발생", e);
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
