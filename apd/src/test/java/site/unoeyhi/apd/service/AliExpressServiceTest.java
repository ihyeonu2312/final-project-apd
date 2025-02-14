package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class AliExpressCrawlerTest {
    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    @BeforeAll
    static void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false) // UI 디버깅을 위해 Headless 모드 해제
                .setArgs(List.of("--disable-blink-features=AutomationControlled"))); // ✅ 탐지 방지

        context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36") // ✅ 탐지 방지
                .setViewportSize(1366, 768)); // 화면 해상도 설정

        page = context.newPage();
    }

    @AfterAll
    static void tearDown() {
        browser.close();
        playwright.close();
    }

    @Test
    void testAliExpressHomePageLoad() {
        page.navigate("https://www.aliexpress.com/");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        String title = page.title();
        System.out.println("📌 페이지 제목: " + title);
        assertNotNull(title);
    }

    @Test
    void testSearchProduct() {
        page.navigate("https://www.aliexpress.com/");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // ✅ 검색창에 'laptop' 입력 후 검색
        page.fill("input[type='search']", "laptop");
        page.keyboard().press("Enter");
        page.waitForTimeout(5000);

        // ✅ 검색 결과 가져오기
        var products = page.querySelectorAll(".manhattan--container--1lP57Ag"); // 상품 리스트 클래스
        System.out.println("📌 검색된 상품 개수: " + products.size());
        assertTrue(products.size() > 0);
    }

    @Test
    void testExtractProductDetails() {
        page.navigate("https://www.aliexpress.com/");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // ✅ 'smartphone' 검색 후 첫 번째 상품 클릭
        page.fill("input[type='search']", "smartphone");
        page.keyboard().press("Enter");
        page.waitForTimeout(5000);

        // ✅ 첫 번째 상품 선택
        var firstProduct = page.querySelector(".manhattan--container--1lP57Ag a");
        assertNotNull(firstProduct);
        firstProduct.click();

        page.waitForTimeout(5000);

        // ✅ 상품명 및 가격 가져오기
        String productName = page.textContent("h1");
        String price = page.textContent(".uniform-banner-box-price");

        System.out.println("📌 상품명: " + productName);
        System.out.println("📌 가격: " + price);

        assertNotNull(productName);
        assertNotNull(price);
    }
}
