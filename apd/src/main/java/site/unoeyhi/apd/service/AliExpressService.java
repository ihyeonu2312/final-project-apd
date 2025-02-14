package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.ProductRepository;

import java.util.*;

@Service
public class AliExpressService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    static final Map<String, String> CATEGORY_MAPPING = Map.of(
        "kr_home_appliances", "APPLIANCES",
        "kr_luggages_&_bags", "BAGS",
        "kr_beauty_x2526_health", "BEAUTY",
        "kr_fashion_accessories", "FASHION",
        "kr_home_x2526_interior", "HOME_INTERIOR",
        "kr_jewelry_x2526_watches", "JEWELRY",
        "kr_sports_x2526_entertainment", "SPORTS"
    );

    public AliExpressService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<String> fetchProductDetails(String url, int maxProducts) {
        System.out.println("🔗 크롤링 시작: URL = " + url + ", maxProducts = " + maxProducts);

        List<String> productNames = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // 페이지 이동 및 로딩 대기
            page.navigate(url);
            page.waitForSelector("[class^='multi--titleText--']");
            page.waitForSelector("[class^='multi--price--']");
            page.waitForSelector("[class^='Categoey--categoryItemTitle--2uJUqT2']");
            page.waitForSelector("[class^='_1IH3l product-img'] img"); // ✅ 이미지 선택자 수정

            // 상품 정보 크롤링
            List<ElementHandle> productElements = page.querySelectorAll("[class^='multi--titleText--']");
            List<ElementHandle> priceElements = page.querySelectorAll("[class^='multi--price--']");
            List<ElementHandle> categoryElements = page.querySelectorAll("[class^='Categoey--categoryItemTitle--2uJUqT2']");
            List<ElementHandle> imageElements = page.querySelectorAll("[class^='_1IH3l product-img'] img"); // ✅ 이미지 선택자 추가

            for (int i = 0; i < Math.min(productElements.size(), maxProducts); i++) {
                String productName = productElements.get(i).innerText().trim();
                String rawPrice = priceElements.size() > i ? priceElements.get(i).innerText().trim() : "0.0";
                double price = parsePrice(rawPrice);
                String aliCategory = categoryElements.size() > i ? categoryElements.get(i).innerText().trim() : "기타";
                String imageUrl = imageElements.size() > i ? imageElements.get(i).getAttribute("src") : null;

                // ✅ AliExpress 카테고리를 React 카테고리로 변환
                String reactCategory = CATEGORY_MAPPING.getOrDefault(aliCategory, null);
                if (reactCategory == null) {
                    System.out.println("⚠️ 카테고리 매칭 실패: " + aliCategory);
                    continue;
                }

                // ✅ category_id 조회 및 설정
                Category category = categoryRepository.findByName(reactCategory)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(reactCategory);
                            return categoryRepository.save(newCategory);
                        });

                // ✅ 상품 저장
                Product product = Product.builder()
                        .name(productName)
                        .description("크롤링된 상품")
                        .price(price)
                        .stockQuantity(100)
                        .imageUrl(imageUrl)
                        .category(category) // ✅ category_id 매핑
                        .build();

                productRepository.save(product);
                productNames.add(productName);

                System.out.println("✅ 상품 저장 완료: " + productName + " | 카테고리: " + reactCategory);
            }

            browser.close();
        } catch (Exception e) {
            System.err.println("❌ 크롤링 중 오류 발생: " + e.getMessage());
        }

        return productNames;
    }

    public double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0.0;
        priceStr = priceStr.replaceAll("[^\\d.]", ""); // 숫자와 '.'만 남기기
        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            System.err.println("❌ 가격 변환 오류: " + priceStr);
            return 0.0;
        }
    }
}
