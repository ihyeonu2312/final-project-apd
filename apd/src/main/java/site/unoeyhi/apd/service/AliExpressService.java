package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

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

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // 페이지 이동 및 로딩 대기
            page.navigate(url);
            page.waitForSelector("[class^='multi--titleText--']");

            // 상품 정보 가져오기
            List<ElementHandle> productElements = page.querySelectorAll("[class^='multi--titleText--']");

            for (int i = 0; i < Math.min(productElements.size(), maxProducts); i++) {
                String productName = productElements.get(i).innerText().trim();
                productNames.add(productName);

                // 🔥 상품 저장 (adminId 없이)
                Product product = Product.builder()
                        .name(productName)
                        .description("크롤링된 상품")
                        .price(0.0)  // 가격 정보 없음
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
}
