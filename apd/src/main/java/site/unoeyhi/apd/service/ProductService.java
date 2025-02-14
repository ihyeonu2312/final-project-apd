package site.unoeyhi.apd.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.dto.ProductDto;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ✅ 모든 상품 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ✅ 특정 카테고리에 속한 모든 상품 조회
    public List<Product> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return productRepository.findByCategory(category); // ✅ 단일 카테고리 처리
    }

    // ✅ 상품 추가 (단일 카테고리 지원)
    public void addProduct(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStockQuantity(productDto.getStockQuantity());

        // ✅ 단일 카테고리 설정
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        product.setCategory(category); // ✅ 단일 카테고리 매핑

        Product savedProduct = productRepository.save(product);

        System.out.println("저장된 상품 ID: " + savedProduct.getProductId());
        System.out.println("상품의 카테고리: " + savedProduct.getCategory().getName());
    }

    // ✅ 크롤링된 상품을 저장 (단일 카테고리 설정)
    // @Transactional
    public void saveCrawledProducts(List<Map<String, String>> productDataList) {
        for (Map<String, String> productData : productDataList) {
            String name = productData.get("name");
            double price = parsePrice(productData.getOrDefault("price", "0.0"));
            String categoryName = productData.get("category");
            String imageUrl = productData.get("imageUrl");

            // ✅ 카테고리 존재 여부 확인 후 없으면 생성
            Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    return categoryRepository.save(newCategory);
                });

            // ✅ 상품 저장 (단일 카테고리 적용)
            Product product = Product.builder()
                    .name(name)
                    .description("크롤링된 상품")
                    .price(price)
                    .stockQuantity(100)
                    .imageUrl(imageUrl)
                    .category(category) // ✅ 단일 카테고리 적용
                    .build();

            productRepository.save(product);
        }
    }

    // ✅ 가격 문자열을 double로 변환하는 메서드
    private double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return 0.0;
        }

        // ✅ 화폐 기호 제거 ($, ₩, €, 등)
        priceStr = priceStr.replaceAll("[^\\d.]", "");

        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            System.err.println("❌ 가격 변환 오류: " + priceStr);
            return 0.0;
        }
    }
}
