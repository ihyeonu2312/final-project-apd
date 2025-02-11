package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AliExpressService {

    public List<String> crawlAliExpressProducts(String url) {
        List<String> productNames = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            // 1️⃣ 브라우저 설정 및 User-Agent 무작위 변경
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(getRandomUserAgent())
            );

            // 2️⃣ 페이지 이동 및 로딩 대기
            Page page = context.newPage();
            page.navigate(url);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // 3️⃣ 동적 요소 대기 후 데이터 수집
            page.waitForSelector(".product-title-text");  // 요소가 로드될 때까지 대기
            List<ElementHandle> productElements = page.querySelectorAll(".product-title-text");

            for (ElementHandle element : productElements) {
                productNames.add(element.innerText().trim());
            }

            // 4️⃣ 브라우저 종료
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
