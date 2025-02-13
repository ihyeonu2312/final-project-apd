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

    // ✅ adminId 없이 동작하는 기본 메서드
    public List<String> fetchProductDetails(String url, int maxProducts) {
        return fetchProductDetails(url, maxProducts, null);  // adminId 없이 실행
    }

    // ✅ adminId를 받을 수 있는 메서드 (추후 관리자 기능 추가 시 사용)
    public List<String> fetchProductDetails(String url, int maxProducts, Long adminId) {
        System.out.println("URL: " + url + ", maxProducts: " + maxProducts + ", adminId: " + adminId);
        
        List<String> productNames = new ArrayList<>();
        List<Map<String, String>> products = new ArrayList<>();  // ✅ 리스트를 메서드 내부에서 초기화
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // 페이지 이동 및 로딩 대기
            page.navigate(url);
            page.waitForSelector("[class^='multi--titleText--']");
            page.waitForSelector("[class^='multi--price--']");
            
            
            // 상품 정보 가져오기
            List<ElementHandle> productElements = page.querySelectorAll("[class^='multi--titleText--']");
            List<ElementHandle> priceElements  = page.querySelectorAll("[class^='multi--price--']");

            for (int i = 0; i < Math.min(productElements.size(), maxProducts); i++) {
                String productName = productElements.get(i).innerText().trim();
                productNames.add(productName);
                // 가격 문자열을 숫자로 변환
                String rawPrice = priceElements.size() > i ? priceElements.get(i).innerText().trim() : "0.0";
                double price = parsePrice(rawPrice);  // ✅ 가격 변환

                Map<String, String> productInfo = new HashMap<>();
                productInfo.put("name", productName);
                productInfo.put("price", String.valueOf(price));

                products.add(productInfo);

                // ✅ 상품 저장 (for 루프 내부에서 실행)
                Product product = Product.builder()
                        .name(productName)
                        .description("크롤링된 상품")
                        .price(price)  // 변환된 가격 사용
                        .stockQuantity(100)  // 기본 재고 설정
                        .build();

                productRepository.save(product);
            }

            System.out.println("✅ 크롤링된 상품들: " + productNames);
            browser.close();
        } catch (Exception e) {
            System.err.println("❌ 크롤링 중 오류 발생: " + e.getMessage());
        }

        return productNames;
    }

    // ✅ 가격 변환 메서드 추가
    private double parsePrice(String priceStr) {
        if (priceStr.equals("가격 없음")) {
            return 0.0; // 가격이 없을 경우 기본값 0.0
        }
        // 화폐 기호 제거 ($, ₩, €, 등)
        priceStr = priceStr.replaceAll("[^\\d.]", ""); 
        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            System.err.println("❌ 가격 변환 오류: " + priceStr);
            return 0.0;
        }
    }
}
