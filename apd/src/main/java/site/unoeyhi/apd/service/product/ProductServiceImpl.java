package site.unoeyhi.apd.service.product;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;


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

         // ✅ 옵션이 null이거나 비어있다면 기본값 추가
    if (productDto.getOptions() == null || productDto.getOptions().isEmpty()) {
        System.out.println("⚠️ [saveProduct] 옵션이 비어있음! 기본값 설정 진행...");
        productDto.setOptions(new ArrayList<>()); // ✅ 빈 리스트 추가
    }
    
    System.out.println("📌 [saveProduct] `saveProduct()`에 전달된 옵션 개수: " + productDto.getOptions().size());
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

            // ✅ 추가 이미지 저장
            saveProductImages(savedProduct, productDto.getAdditionalImages());

            // ✅ 옵션 저장 (여기서 한 번 더 검증)
            if (productDto.getOptions().isEmpty()) {
                System.out.println("⚠️ [saveProduct] 옵션이 비어있음! 기본값으로 빈 옵션 리스트 처리...");
            }
            saveProductOptions(savedProduct, productDto.getOptions());

            System.out.println("✅ [saveProduct] 상품, 이미지, 옵션 최종 저장 완료");
            return savedProduct;

        } catch (Exception e) {
            System.out.println("🚨 [saveProduct] 상품 저장 실패: " + e.getMessage());
            e.printStackTrace();
            throw e; //트랜잭션 자동 롤백
        }
    }

    // ✅ 추가 이미지 저장을 별도의 메서드로 분리
    private void saveProductImages(Product savedProduct, List<String> additionalImages) {
        if (additionalImages == null || additionalImages.isEmpty()) {
            System.out.println("⚠️ [saveProduct] 추가 이미지가 없습니다!");
            return;
        }

        boolean isFirstImage = true;
        for (String imageUrl : additionalImages) {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                ProductImage productImage = ProductImage.builder()
                    .imageUrl(imageUrl)
                    .isThumbnail(isFirstImage) // ✅ boolean 값 전달
                    .product(savedProduct)
                    .build();
                productImageRepository.save(productImage);
                isFirstImage = false; // ✅ 이후부터는 썸네일 X
            }
        }
    }
        // ✅ 옵션 저장을 별도의 메서드로 분리
        private void saveProductOptions(Product savedProduct, List<OptionDto> optionDtos) {
            if (optionDtos == null || optionDtos.isEmpty()) {
                System.out.println("⚠️ [saveProductOptions] 옵션이 없음! 상품 ID: " + savedProduct.getProductId());
                return;
            }
    
            System.out.println("📌 [saveProduct] 옵션 개수 확인: " + optionDtos.size());
    
            // ✅ 저장할 ProductOption 리스트
            List<ProductOption> productOptionList = new ArrayList<>();
    
            Set<String> excludedOptions = new HashSet<>(Arrays.asList(
                "전체", "패션의류/잡화", "뷰티", "출산/유아동", "식품", "주방용품", "생활용품", "홈인테리어", "가전디지털",
                "스포츠/레저", "자동차용품", "도서/음반/DVD", "완구/취미", "문구/오피스", "반려동물용품", "헬스/건강식품",
                "국내여행", "해외여행", "R.LUX", "로켓설치", "쿠팡 프리미엄", "공간별 집꾸미기", "헬스케어 전문관",
                "쿠팡 Only", "싱글라이프", "악기전문관", "결혼준비", "아트/공예", "미세먼지용품", "홈카페", "실버스토어", "로켓펫닥터"
            ));
    
            for (OptionDto optionDto : optionDtos) {
                String optionValue = optionDto.getOptionValue().trim();
    
                // ✅ 제외할 옵션 필터링
                if (excludedOptions.contains(optionValue) || optionValue.isEmpty()) {
                    System.out.println("⚠️ [제외됨] 불필요한 옵션 제외: " + optionValue);
                    continue;
                }
                if (optionValue.contains("선택하신") || optionValue.contains("없는 사이즈")) {
                    continue;
                }
    
                System.out.println("🛠️ 저장할 옵션: " + optionDto.getOptionValueType() + " - " + optionValue);
    
                // ✅ 기존 옵션이 있는지 확인
                Optional<Option> existingOption = optionRepository.findByOptionValueTypeAndOptionValue(
                        optionDto.getOptionValueType(), optionValue);
    
                Option option = existingOption.orElseGet(() -> {
                    Option newOption = Option.builder()
                        .optionValueType(optionDto.getOptionValueType())
                        .optionValue(optionValue)
                        .build();
                    System.out.println("✅ [DB 저장] 새로운 옵션 생성: " + newOption.getOptionValue());
                    return optionRepository.save(newOption);
                });
    
                // ✅ ProductOption 생성 후 리스트에 추가
                ProductOption productOption = ProductOption.builder()
                    .product(savedProduct)
                    .option(option)
                    .build();
                productOptionList.add(productOption);
            }
    
            // ✅ Batch Insert 적용 (옵션 리스트 한 번에 저장)
            if (!productOptionList.isEmpty()) {
                productOptionRepository.saveAll(productOptionList);
                System.out.println("✅ [Batch Insert] " + productOptionList.size() + "개 옵션 한 번에 저장 완료.");
            }
    
            // ✅ 중간 flush()로 메모리 비우기 (옵션 개수가 많을 때)
            entityManager.flush();
            entityManager.clear();
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
