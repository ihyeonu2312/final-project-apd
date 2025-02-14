package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
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

    public AliExpressService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public void crawlAndSaveProducts(String url, int maxProducts) {
        System.out.println("🔗 크롤링 시작: " + url);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate(url);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(5000);

            List<ElementHandle> productElements = page.querySelectorAll("div[title]");
            List<ElementHandle> priceElements = page.querySelectorAll(".manhattan--price-sale--1CCSZ");
            List<ElementHandle> imageElements = page.querySelectorAll("img[class*='product-img']");
            List<ElementHandle> categoryElements = page.querySelectorAll("[class^='Category--categoryItemTitle']");

            System.out.println("🔍 크롤링된 상품 개수: " + productElements.size());

            for (int i = 0; i < Math.min(productElements.size(), maxProducts); i++) {
                String productName = productElements.get(i).getAttribute("title");

                // ✅ 가격 가져오기 (기존 방식)
                String priceText = priceElements.size() > i ? priceElements.get(i).innerText() : "0";

                // ✅ 가격이 0이거나 비어 있다면 iframe에서 가져오기
                if (priceText.equals("0") || priceText.isEmpty()) {
                    System.out.println("⚠️ 가격 정보가 비어 있음, iframe에서 가져오기 시도...");
                    List<Frame> frames = page.frames();
                    for (Frame frame : frames) {
                        try {
                            if (frame.locator("div.U-S0J span").count() > 0) {
                                priceText = frame.locator("div.U-S0J span").innerText();
                                System.out.println("✅ 찾은 가격 (iframe): " + priceText);
                                break;
                            }
                        } catch (Exception e) {
                            System.err.println("⚠️ `iframe` 접근 오류 발생: " + e.getMessage());
                        }
                    }
                }

                double price = parsePrice(priceText);
                String imageUrl = imageElements.size() > i ? imageElements.get(i).getAttribute("src") : null;

                // ✅ 카테고리 매칭
                String aliCategory = categoryElements.size() > i ? categoryElements.get(i).innerText().trim() : "기타";
                String reactCategory = CATEGORY_MAPPING.getOrDefault(aliCategory, "기타");

                // ✅ DB에서 카테고리 조회
                Category category = categoryRepository.findByName(reactCategory)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(reactCategory);
                            return categoryRepository.save(newCategory);
                        });

                // ✅ 중복 상품 확인 (DB에서 같은 이름의 상품이 있는지 검사)
                Optional<Product> existingProduct = productRepository.findByName(productName);
                if (existingProduct.isPresent()) {
                    System.out.println("⚠️ 중복 상품 발견: " + productName + " (저장 안 함)");
                    continue;
                }

                // ✅ 가격이 0이면 기본값 설정
                if (price == 0.0) {
                    price = 9.99;
                }

                // ✅ 저장 시도 로그 추가
                System.out.println("🛠 저장 시도: " + productName + " | 💰 " + price + " | 📦 카테고리: " + reactCategory);

                // ✅ 상품 저장
                Product product = Product.builder()
                        .name(productName)
                        .description("AliExpress 크롤링 상품")
                        .price(price)
                        .stockQuantity(100)
                        .category(category)
                        .imageUrl(imageUrl)
                        .build();

                productRepository.save(product);
                System.out.println("✅ 저장 완료: " + productName + " | 💰 " + price + " | 📦 카테고리: " + reactCategory);
            }

            browser.close();
        } catch (Exception e) {
            System.err.println("❌ 크롤링 중 오류 발생: " + e.getMessage());
        }
    }

    private static final Map<String, String> CATEGORY_MAPPING = Map.of(
        "kr_home_appliances", "APPLIANCES",
        "kr_luggages_&_bags", "BAGS",
        "kr_beauty_&_health", "BEAUTY",
        "kr_fashion_accessories", "FASHION",
        "kr_home_&_interior", "HOME_INTERIOR",
        "kr_jewelry_&_watches", "JEWELRY",
        "kr_sports_&_entertainment", "SPORTS"
    );

    private double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0.0;
        priceStr = priceStr.replaceAll("[^\\d.]", ""); // 숫자와 '.'만 남김
        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            System.err.println("❌ 가격 변환 오류: " + priceStr);
            return 0.0;
        }
    }
}
