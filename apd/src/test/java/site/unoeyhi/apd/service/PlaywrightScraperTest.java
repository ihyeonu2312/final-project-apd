package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class PlaywrightScraperTest {
    // ✅ 스크롤을 자동으로 내리는 함수 개선 (동적 스크롤 적용)
    private void scrollPage(Page page) {
        for (int i = 0; i < 10; i++) {  // 10번 스크롤
            page.evaluate("window.scrollBy(0, window.innerHeight / 2)");  // 반 화면씩 스크롤
            page.waitForTimeout(1000);  // 1초 대기 (상품 로딩 시간 확보)
        }
    }

    static Playwright playwright;
    static Browser browser;

    @BeforeAll
    static void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(false)
            .setSlowMo(500));  // 속도 조절
    }

    @AfterAll
    static void teardown() {
        browser.close();
        playwright.close();
    }

    @Test
    void testScrapeProductDetails() {
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36"));

        Page page = context.newPage();
        page.navigate("https://ko.aliexpress.com/");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // ✅ AliExpress의 팝업 닫기
        try { page.locator(".pop-close-btn").click(); } catch (Exception ignored) {}
        try { page.locator("._24EHh").click(); } catch (Exception ignored) {}
        try { page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName("close")).click(); } catch (Exception ignored) {}

        // ✅ "유아용품" 카테고리 클릭
        System.out.println("유아용품 카테고리 클릭...");
        Page newPage;
        try {
            newPage = page.waitForPopup(() -> {
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("유아용품")).first().click();
            });

            if (newPage == null) {
                newPage = page;  // 새 창이 열리지 않으면 기존 페이지 사용
            }
        } catch (Exception e) {
            System.out.println("새 창을 기다리는 동안 오류 발생: " + e.getMessage());
            newPage = page;  // 예외 발생 시 기존 페이지 사용
        }

        // ✅ 새 창에서 팝업 닫기
        try { newPage.locator("._24EHh").click(); } catch (Exception ignored) {}
        try { newPage.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName("close")).click(); } catch (Exception ignored) {}

        // ✅ 새 창에서 스크롤 실행
        System.out.println("새 창에서 스크롤 진행...");
        scrollPage(newPage);

        // ✅ 상품 목록이 로드될 때까지 대기
        System.out.println("상품 목록 로딩 중...");
        for (int i = 1; i <= 10; i++) {
            newPage.waitForSelector("#root > div > div > div:nth-child(3) > div:nth-child(2) > div > div:nth-child(" + i + ")", 
                new Page.WaitForSelectorOptions().setTimeout(15000));
        }

        // ✅ 첫 번째 상품 클릭 후 현재 페이지에서 이동
        System.out.println("첫 번째 상품 클릭 후 현재 페이지에서 이동...");
        newPage.locator("#root > div > div > div:nth-child(3) > div:nth-child(2) > div > div:nth-child(1) a").click();

        // ✅ 상세 페이지가 완전히 로드될 때까지 대기
        newPage.waitForLoadState(LoadState.NETWORKIDLE);

        // ✅ 상세 페이지에서 추가 스크롤 (필요할 경우)
        scrollPage(newPage);

        // ✅ 상품명 가져오기
        String productName = newPage.locator("h1[data-pl='product-title']").textContent();

        // ✅ 가격 가져오기
        String price = newPage.locator("span[class*='product-price-value']").textContent();

        // ✅ 콘솔 출력
        System.out.println("상품명: " + productName);
        System.out.println("가격: " + price);

        // ✅ 데이터 검증
        assertNotNull(productName, "상품명이 null입니다.");
        assertNotNull(price, "가격이 null입니다.");
    }
}
