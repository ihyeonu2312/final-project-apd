package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AliExpress {
    public List<String> crawlAliExpressProducts(String url) {
        List<String> productNames = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true)); // 필요하면 false로 변경
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
            );
            Page page = context.newPage();
            // ✅ 페이지 이동
            page.navigate(url);
            page.waitForLoadState(LoadState.LOAD);

            // ✅ 제품명 가져오기
            List<ElementHandle> productElements = page.querySelectorAll(".product-title-text");
            for (ElementHandle element : productElements) {
                productNames.add(element.innerText().trim());
            }

            // ✅ 브라우저 종료
            browser.close();
        } catch (Exception e) {
            System.err.println("❌ Playwright 크롤링 중 오류 발생: " + e.getMessage());
        }

        return productNames;
    }
}
