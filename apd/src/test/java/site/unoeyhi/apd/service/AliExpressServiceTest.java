package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import site.unoeyhi.apd.model.CategoryModel;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AliExpressServiceTest {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    @BeforeAll
    static void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false) // UI 디버깅을 위해 Headless 모드 해제
                .setArgs(List.of(
                        "--disable-blink-features=AutomationControlled", // ✅ 자동화 탐지 방지
                        "--disable-web-security", "--disable-site-isolation-trials",
                        "--disable-features=IsolateOrigins,site-per-process" // ✅ 크롤링 차단 방지
                )));

        context = browser.newContext(new Browser.NewContextOptions()
                .setBypassCSP(true) // ✅ 크롤링 차단 우회
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36") // ✅ 탐지 방지
                .setViewportSize(1366, 768)); // 화면 해상도 설정

        page = context.newPage();
    }

    @Test
    void ScrapAliExpress() {
        List<CategoryModel> categoryList = new ArrayList<>();

        // ✅ 페이지 이동
        page.navigate("https://www.aliexpress.com/",
            new Page.NavigateOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(10000); // ✅ 추가 대기
        System.out.println("✅ 페이지 이동 완료");

        /// 1. 🔍 AliExpress의 봇 감지 회피
        page.evaluate("() => navigator.webdriver = false");

        /// 1. 🔍 페이지의 모든 `ul` 태그 확인 (디버깅)
        List<Locator> allLists = page.locator("ul").all();
        System.out.println("📌 페이지 내 모든 ul 태그 개수: " + allLists.size());

        for (Locator list : allLists) {
            System.out.println("🔍 ul 태그 내용: " + list.innerHTML());
        }
          /// 3. ✅ 요소가 로드될 때까지 대기
        System.out.println("🔵 대분류 카테고리 대기 시작");
        try {
            page.waitForSelector("ul.Category--categoryList--2QES_k6 > a > li ",
                new Page.WaitForSelectorOptions().setTimeout(50000)); // ✅ 요소 로드 대기
        } catch (Exception e) {
            System.out.println("⚠️ 요소 로드 실패: " + e.getMessage());
            return;
        }
        System.out.println("✅ 요소 로드 완료");

        /// 2. 🔍 `ul.Categoey--categoryList--2QES_k6` 내부 `li` 태그 확인
        Locator categoryItems = page.locator("ul.Categoey--categoryList--2QES_k6 > a > li");
        categoryItems.scrollIntoViewIfNeeded();
        int categoryCount = categoryItems.count();
        System.out.println("📌 'ul.Categoey--categoryList--2QES_k6' 내 li 개수: " + categoryCount);

        for (int i = 0; i < categoryCount; i++) {
            System.out.println("🔍 li[" + i + "] HTML: " + categoryItems.nth(i).innerHTML());
        }

        /// 3. 🔍 iframe 존재 여부 확인
        List<Frame> frames = page.frames();
        System.out.println("📌 현재 페이지 내 iframe 개수: " + frames.size());

        for (Frame frame : frames) {
            System.out.println("🔍 iframe URL: " + frame.url());
        }

        /// 4. ✅ AliExpress의 봇 감지 회피
        page.evaluate("() => navigator.webdriver = false");

        /// 5. 대분류 카테고리 크롤링
        System.out.println("🔵 대분류 카테고리 스크랩 시작");
        try {
            if (categoryCount > 0) {
                System.out.println("✅ 대분류 카테고리 개수: " + categoryCount);
                for (int i = 0; i < categoryCount; i++) {
                    Locator item = categoryItems.nth(i);
                    Locator link = item.locator("a");
                    String categoryName = item.getAttribute("data"); // ✅ `li`의 `data` 속성에서 이름 가져오기
                    String categoryUrl = (link.count() > 0) ? link.first().getAttribute("href") : null;

                    if (categoryName != null && categoryUrl != null) {
                        CategoryModel newCategory = new CategoryModel();
                        newCategory.setCategoryName(categoryName);
                        newCategory.setCategoryUrl(categoryUrl);
                        categoryList.add(newCategory);
                        System.out.println("✅ 카테고리 추가됨: " + categoryName + " | " + categoryUrl);
                    } else {
                        System.out.println("⚠️ 데이터가 부족한 카테고리 발견: " + categoryName);
                    }
                }
                System.out.println("✅ 대분류 카테고리 스크랩 완료");
            } else {
                System.out.println("⚠️ 대분류 카테고리를 찾을 수 없음");
            }
        } catch (Exception e) {
            System.out.println("⚠️ 대분류 카테고리 스크랩 중 오류 발생: " + e.getMessage());
        }

        System.out.println("📌 최종 대분류 카테고리 결과: " + categoryList);
        browser.close();
    }
}

