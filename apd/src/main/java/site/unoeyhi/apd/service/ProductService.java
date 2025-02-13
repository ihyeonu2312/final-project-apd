package site.unoeyhi.apd.service;

import java.util.ArrayList;
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


    //모든 상품 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
      // ✅ 특정 카테고리에 속한 모든 상품 조회
      public List<Product> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return productRepository.findByCategories(category);
    }

    // 상품 추가 (복수 카테고리 지원)
    public void addProduct(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStockQuantity(productDto.getStockQuantity());

      // 카테고리 설정: 리스트로 변환 후 조회
      List<Category> categories = categoryRepository.findAllById(productDto.getCategoryIds());
      
       // ✅ 조회된 카테고리가 없는 경우 예외 처리
    if (categories.isEmpty()) {
        throw new RuntimeException("카테고리를 찾을 수 없습니다. 상품을 추가할 수 없습니다.");
    }
      product.setCategories(categories); 
      categories.forEach(category -> category.getProducts().add(product)); // 🔥 양방향 관계 유지

      Product savedProduct = productRepository.save(product);

      System.out.println("저장된 상품 ID: " + savedProduct.getProductId());
      System.out.println("상품의 카테고리 목록: " + savedProduct.getCategories());
    }

      // ✅ 크롤링된 상품을 저장 (기본 카테고리 설정)
      @Transactional
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
      
              // ✅ 상품 저장
              Product product = Product.builder()
                      .name(name)
                      .description("크롤링된 상품")
                      .price(price)
                      .stockQuantity(100)
                      .imageUrl(imageUrl) // ✅ 이미지 저장
                      .categories(List.of(category)) // ✅ 카테고리 할당
                      .build();
      
              productRepository.save(product);
          }
      }
      // 가격 문자열을 double로 변환하는 메서드
    private double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return 0.0; // ✅ null 또는 빈 문자열일 경우 기본값
        }

        // ✅ 화폐 기호 제거 ($, ₩, €, 등)
        priceStr = priceStr.replaceAll("[^\\d.]", "");

        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            System.err.println("❌ 가격 변환 오류: " + priceStr);
            return 0.0; // ✅ 변환 실패 시 기본값
        }
    }
    
}
