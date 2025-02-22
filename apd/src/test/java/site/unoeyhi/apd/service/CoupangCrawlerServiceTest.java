package site.unoeyhi.apd.service;


import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class CoupangCrawlerServiceTest {

    @Autowired
    private CategoryRepository categoryRepository; // JPA Repository

     @Test
    void testCrawlCoupangCategories() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setExtraHTTPHeaders(Map.of(
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Accept-Language", "ko-KR,ko;q=0.9",
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
            )));
            Page page = context.newPage();

            // 콘솔 메시지 출력 (디버깅용)
            page.onConsoleMessage(msg -> System.out.println("Console: " + msg.text()));

            page.navigate("https://www.coupang.com/");
            page.waitForLoadState(LoadState.LOAD); // 페이지가 완전히 로드될 때까지 대기

            String title = page.title();
            System.out.println("페이지 타이틀: " + title);

            // 페이지가 정상적으로 로드되었는지 확인
            assertNotNull(title, "페이지 타이틀이 null입니다.");
            assertFalse(title.isEmpty(), "페이지 타이틀이 비어 있습니다.");

            browser.close();
        }
    }

    @Test
    void testCrawlAndSaveCategories() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setExtraHTTPHeaders(Map.of(
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Accept-Language", "ko-KR,ko;q=0.9",
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
            )));
            Page page = context.newPage();
    
            // ✅ 쿠팡 페이지 이동 후 로딩 기다리기
            page.navigate("https://www.coupang.com/");
            page.waitForLoadState(LoadState.NETWORKIDLE); // 네트워크 요청이 끝날 때까지 대기

            // ✅ 2. 햄버거 메뉴 클릭 (카테고리가 숨겨져 있을 가능성 있음)
            page.locator("div.category-btn").click();
    
            // ✅ 3. 카테고리 목록이 나타날 때까지 대기
            Locator categoryLocator = page.locator("ul.shopping-menu-list li a.first-depth");
            categoryLocator.first().waitFor(new Locator.WaitForOptions().setTimeout(60000)); // 60초 대기

            // ✅ 대분류 카테고리 가져오기
            List<ElementHandle> categoryElements = page.querySelectorAll("ul.shopping-menu-list li a.first-depth");
            System.out.println("카테고리 개수: " + categoryElements.size());
    
            if (categoryElements.isEmpty()) {
                throw new RuntimeException("카테고리를 찾지 못했습니다. CSS 선택자를 확인하세요.");
            }
    
            // ✅ 데이터 저장 (중복 방지)
            for (ElementHandle element : categoryElements) {
                String categoryName = element.innerText().trim();
                String categoryUrl = element.getAttribute("href");
                String coupangCategoryId = extractCategoryId(categoryUrl);
    
                // 중복 저장 방지
                if (categoryRepository.findByCategoryName(categoryName).isPresent()) {
                    System.out.println("이미 존재하는 카테고리: " + categoryName);
                    continue;
                }
    
                Category category = Category.builder()
                        .categoryName(categoryName)
                        .coupangCategoryId(coupangCategoryId)
                        .url(categoryUrl)
                        .build();
    
                categoryRepository.save(category);
                System.out.println("저장 완료: " + categoryName);
            }
    
            // ✅ DB 저장 확인
            List<Category> savedCategories = categoryRepository.findAll();
            assertThat(savedCategories).isNotEmpty();
            assertThat(savedCategories.size()).isGreaterThan(0);
    
            browser.close();
        }
    }
    
    // ✅ 쿠팡 카테고리 ID 추출 (URL에서 숫자만 추출)
    private String extractCategoryId(String url) {
        if (url == null) return null;
        return url.replaceAll("[^0-9]", ""); // 숫자만 남기기
    }
}
    
