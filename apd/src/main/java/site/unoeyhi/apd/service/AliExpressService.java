package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AliExpressService {

    public List<String> fetchProductDetails(String url, int maxProducts) {
        System.out.println("URL: " + url + ", maxProducts: " + maxProducts);  // 요청 로그 출력
        List<String> productNames = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // 페이지 이동 및 로딩 대기
            page.navigate(url);
            page.waitForSelector("[class^='multi--titleText--']");
            // page.waitForLoadState(LoadState.NETWORKIDLE);

            // 상품 정보 가져오기
            List<ElementHandle> productElements = page.querySelectorAll("[class^='multi--titleText--']");

            // 🔥 상품 개수 제한 적용
            for (int i = 0; i < Math.min(productElements.size(), maxProducts); i++) {
                productNames.add(productElements.get(i).innerText().trim());
            }
            System.out.println("크롤링된 상품들: " + productNames);
            browser.close();
        } catch (Exception e) {
            System.err.println("❌ 크롤링 중 오류 발생: " + e.getMessage());
        }

        return productNames;
    }
    // ✅ 무작위 User-Agent 제공 메서드
    private String getRandomUserAgent() {
        List<String> userAgents = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/537.36",
            "Mozilla/5.0 (iPad; CPU OS 16_5 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/537.36"
        );
        Collections.shuffle(userAgents);
        return userAgents.get(0);
    }
}
    


