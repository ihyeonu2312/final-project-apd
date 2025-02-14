package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.ProductRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AliExpressService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    static final Map<String, String> CATEGORY_MAPPING = Map.of(
        "kr_home_appliances", "APPLIANCES"
        // "kr_luggages_x2526_bags", "BAGS",
        // "kr_beauty_x2526_health", "BEAUTY",
        // "kr_fashion_accessories", "FASHION",
        // "kr_home_x2526_interior", "HOME_INTERIOR",
        // "kr_jewelry_x2526_watches", "JEWELRY",
        // "kr_sports_x2526_entertainment", "SPORTS"
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

            // ✅ 페이지 이동 및 로딩 대기
            page.navigate(url);
            page.waitForSelector("div[title]"); // ✅ 상품명 선택자
            page.waitForSelector("div[class*='U-S0J'] span"); // ✅ 가격 선택자
            page.waitForSelector("[class^='Category--categoryItem']"); // ✅ 카테고리 선택자
            page.waitForSelector("img[class*='product-img']"); // ✅ 이미지 선택자
            page.waitForSelector("div.1okBC"); // ✅ 숫자로 시작하는 클래스 선택자 (수정됨)
            page.waitForTimeout(5000); // ✅ 추가 로딩 대기

            // 스크롤 이동 (가격이 화면에 보이도록)
            Locator priceLocator = page.locator("div[class*='U-S0J'] span");
            priceLocator.scrollIntoViewIfNeeded(); // ✅ 요소가 화면에 있도록 스크롤


            // ✅ 상품 정보 크롤링
            List<ElementHandle> productElements = page.querySelectorAll("div[title]"); // 상품명
            List<ElementHandle> priceElements = page.querySelectorAll("div.1okBC span"); // 가격 (span 태그 포함)
            List<ElementHandle> categoryElements = page.querySelectorAll("[class^='Category--categoryItem']"); // 카테고리
            List<ElementHandle> imageElements = page.querySelectorAll("img[class*='product-img']"); // 이미지

            // ✅ 크롤링된 요소 개수 출력 (디버깅용)
            System.out.println("🔍 상품 개수: " + productElements.size());
            System.out.println("🔍 가격 개수: " + priceElements.size());
            System.out.println("🔍 카테고리 개수: " + categoryElements.size());
            System.out.println("🔍 이미지 개수: " + imageElements.size());

            for (int i = 0; i < Math.min(productElements.size(), maxProducts); i++) {
                String productName = productElements.get(i).getAttribute("title").trim(); // ✅ 상품명 가져오기
                String rawPrice = priceElements.size() > i ? priceElements.get(i).innerText().trim() : "0.0"; // ✅ 가격 가져오기
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
                        .category(category) // ✅ category_id 매핑
                        .imageUrl(imageUrl)
                        .build();

                productRepository.save(product);
                System.out.println("✅ 상품 저장 완료: " + productName + " | 가격: " + price + " | 카테고리: " + reactCategory);
            }


            browser.close();
        } catch (Exception e) {
            System.err.println("❌ 크롤링 중 오류 발생: " + e.getMessage());
        }

        return productNames;
    }

    // ✅ 카테고리명 디코딩 메서드 추가
    public String decodeAliCategory(String encodedCategory) {
        if (encodedCategory == null) return "기타";
        return URLDecoder.decode(encodedCategory.replace("x2526", "&"), StandardCharsets.UTF_8);
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
