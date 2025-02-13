package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.ProductRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AliExpressService {

    private final ProductRepository productRepository;

    public AliExpressService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<String> fetchProductDetails(String url, int maxProducts ,Long adminId) {
        System.out.println("URL: " + url + ", maxProducts: " + maxProducts +  ", adminId: " + adminId);

        List <String> productNames = new ArrayList<>();
        List<Map<String, String>> productDataList = new ArrayList<>(); // ✅ 크롤링한 데이터 저장

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // 페이지 이동
            page.navigate(url);
            page.waitForSelector("[class^='multi--titleText--']");
            page.waitForSelector("[class^='multi--price--']");
            page.waitForSelector("[class^='Categoey--categoryItemTitle--']");
            page.waitForSelector("[class^='_1IH3l product-img'] img"); // ✅ 이미지 선택자

            List<ElementHandle> productElements = page.querySelectorAll("[class^='multi--titleText--']");
            List<ElementHandle> priceElements = page.querySelectorAll("[class^='multi--price--']");
            List<ElementHandle> categoryElements = page.querySelectorAll("[class^='Categoey--categoryItemTitle--']");
            List<ElementHandle> imageElements = page.querySelectorAll("[class^='_1IH3l product-img"); // ✅ 이미지 리스트 가져오기

            for (int i = 0; i < Math.min(productElements.size(), maxProducts); i++) {
                String productName = productElements.get(i).innerText().trim();
                String rawPrice = priceElements.size() > i ? priceElements.get(i).innerText().trim() : "0.0";
                double price = parsePrice(rawPrice);
                String categoryName = categoryElements.size() > i ? categoryElements.get(i).innerText().trim() : "기타";
                String imageUrl = imageElements.size() > i ? imageElements.get(i).getAttribute("src") : null; // ✅ 이미지 URL 추출

                Map<String, String> productInfo = new HashMap<>();
                productInfo.put("name", productName);
                productInfo.put("price", String.valueOf(price));
                productInfo.put("category", categoryName);
                productInfo.put("imageUrl", imageUrl); // ✅ 이미지 추가

                productDataList.add(productInfo);

                Product product = Product.builder()
                        .name(productName)
                        .description("크롤링된 상품")
                        .price(price)
                        .stockQuantity(100)
                        .imageUrl(imageUrl) // ✅ 이미지 저장
                        .build();

                productRepository.save(product);
                productNames.add(productName);
            }

            browser.close();
        } catch (Exception e) {
            System.err.println("❌ 크롤링 중 오류 발생: " + e.getMessage());
        }

        return productNames;
    }

    private double parsePrice(String priceStr) {
        if (priceStr.equals("가격 없음")) return 0.0;
        priceStr = priceStr.replaceAll("[^\\d.]", "");
        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            System.err.println("❌ 가격 변환 오류: " + priceStr);
            return 0.0;
        }
    }
}

