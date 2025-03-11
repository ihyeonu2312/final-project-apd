package site.unoeyhi.apd.service.product;

import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import site.unoeyhi.apd.dto.product.OptionDto;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Discount;
import site.unoeyhi.apd.entity.Option;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductImage;
import site.unoeyhi.apd.entity.ProductOption;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductImageRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;
import site.unoeyhi.apd.repository.product.DiscountRepository;
import site.unoeyhi.apd.repository.product.OptionRepository;
import site.unoeyhi.apd.repository.product.ProductOptionRepository;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final OptionRepository optionRepository;
    private final ProductOptionRepository productOptionRepository;
    private final DiscountRepository discountRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                              ProductImageRepository productImageRepository, OptionRepository optionRepository,
                              ProductOptionRepository productOptionRepository, DiscountRepository discountRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.optionRepository = optionRepository;
        this.productOptionRepository = productOptionRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    public Product saveProduct(ProductDto productDto) {
        System.out.println("🚀 [saveProduct] 상품 저장 시작: " + productDto.getName());

        // ✅ 필수값 체크
        if (productDto.getName() == null || productDto.getName().isEmpty()) return null;
        if (productDto.getPrice() == null || productDto.getPrice() <= 0) return null;
        if (productDto.getImageUrl() == null || productDto.getImageUrl().isEmpty()) return null;

        try {
            // ✅ 카테고리 확인
            Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 카테고리 ID"));

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
            entityManager.flush();
            entityManager.clear();

            // ✅ 추가 이미지 저장
            saveProductImages(savedProduct, productDto.getAdditionalImages());

            // ✅ 옵션 저장
            saveProductOptions(savedProduct, productDto.getOptions());

            // ✅ 할인 저장
            double discountPrice = (productDto.getDiscountPrice() != null) ? productDto.getDiscountPrice() : 0.0;
            saveProductDiscount(savedProduct, "fixed", discountPrice);


            return savedProduct;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ 추가 이미지 저장
    @Override
    public void saveProductImage(Long productId, String imageUrl, boolean isThumbnail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없음: " + productId));
    
        ProductImage productImage = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .isThumbnail(isThumbnail)
                .build();
    
        productImageRepository.save(productImage);
    }
    
    private void saveProductImages(Product savedProduct, List<String> additionalImages) {
        if (additionalImages == null || additionalImages.isEmpty()) return;
    
        boolean isFirstImage = true;
        for (String imageUrl : additionalImages) {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                saveProductImage(savedProduct.getProductId(), imageUrl, isFirstImage);
                isFirstImage = false;
            }
        }
    }
    

    // ✅ 할인 정보 저장
    @Override
    public void saveProductDiscount(Product product, String discountType, double discountPrice) {
        if (discountPrice == 0) return;

        Discount discount = Discount.builder()
                .product(product)
                .discountType(discountType)
                .discountValue(discountPrice)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();

        discountRepository.save(discount);
    }


    // ✅ 옵션 저장
    @Override
    public void saveProductOption(Long productId, OptionDto optionDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없음: " + productId));

        Option option = optionRepository.findByOptionValueTypeAndOptionValue(optionDto.getOptionValueType(), optionDto.getOptionValue())
                .orElseGet(() -> optionRepository.save(Option.builder()
                        .optionValueType(optionDto.getOptionValueType())
                        .optionValue(optionDto.getOptionValue())
                        .build()));

        ProductOption productOption = ProductOption.builder()
                .product(product)
                .option(option)
                .build();

        productOptionRepository.save(productOption);
    }

    private void saveProductOptions(Product savedProduct, List<OptionDto> optionDtos) {
        if (optionDtos == null || optionDtos.isEmpty()) return;

        for (OptionDto optionDto : optionDtos) {
            saveProductOption(savedProduct.getProductId(), optionDto);
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
