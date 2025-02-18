package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;

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

        page.navigate("https://www.aliexpress.com/",
            new Page.NavigateOptions().setTimeout(60000));
        page.waitForTimeout(3000);
        System.out.println("✅ 페이지 이동 완료");

        /// 1. 팝업 제거 (강제 숨김 + ESC 키 활용)
        System.out.println("🔵 팝업 제거 시작");
        try {
            // ✅ 모든 팝업 숨김 (CSS 스타일 적용)
            page.evaluate("() => document.body.setAttribute('automation-controlled', 'false')");


        // ✅ ESC 키로 팝업 닫기
        page.keyboard().press("Escape");
        page.waitForTimeout(2000);

        // ✅ 닫기 버튼 강제 클릭
        Locator closeButtons = page.locator("img[alt='close']");
        if (closeButtons.count() > 0) {
            for (int i = 0; i < closeButtons.count(); i++) {
                closeButtons.nth(i).scrollIntoViewIfNeeded();
                closeButtons.nth(i).click(new Locator.ClickOptions().setForce(true));
                page.waitForTimeout(1000);
            }
        }
        System.out.println("✅ 팝업 제거 완료");

        } catch (Exception e) {
            System.out.println("⚠️ 팝업 제거 중 오류 발생: " + e.getMessage());
        }

        /// 2. 카테고리 메뉴 열기
        System.out.println("🔵 카테고리 메뉴 열기");
        try {
           // ✅ 카테고리 메뉴 클릭 강제 실행
        Locator categoryMenuButton = page.locator("div[data-spm='allcategoriespc']");
        page.waitForSelector("div[data-spm='allcategoriespc']", 
            new Page.WaitForSelectorOptions().setTimeout(10000));

        if (categoryMenuButton.isVisible() && categoryMenuButton.isEnabled()) {
            categoryMenuButton.scrollIntoViewIfNeeded();
            categoryMenuButton.click(new Locator.ClickOptions().setForce(true));
            page.waitForTimeout(5000);
            System.out.println("✅ 카테고리 메뉴 클릭 성공!");
        } else {
            System.out.println("⚠️ 카테고리 버튼이 비활성화됨");
        }

        } catch (Exception e) {
            System.out.println("⚠️ 카테고리 클릭 중 오류 발생: " + e.getMessage());
            return;
        }

        /// 3. 대분류 카테고리 크롤링
        System.out.println("🔵 대분류 카테고리 스크랩 시작");
        try {
            // 🔍 **디버깅용 - HTML 내용 확인**
            String categoryHtml = page.innerHTML("ul.Categoey--categoryList--2QES_k6");
            System.out.println("📌 카테고리 HTML 내용: " + categoryHtml);

            List<Frame> frames = page.frames();
            System.out.println("📌 현재 페이지 내 iframe 개수: " + frames.size());

            for (Frame frame : frames) {
                System.out.println("🔍 iframe URL: " + frame.url());
            }
            page.evaluate("() => document.body.setAttribute('automation-controlled', 'false')");
            page.waitForSelector("ul.Categoey--categoryList--2QES_k6 > a:visible",
                new Page.WaitForSelectorOptions().setTimeout(30000)); // ⬆️ 30초로 증가

            Locator categories = page.locator("ul.Categoey--categoryList--2QES_k6 > a:visible");

            int categoryCount = categories.count();
            if (categoryCount > 0) {
                System.out.println("✅ 대분류 카테고리 개수: " + categoryCount);
                categories.all().forEach(category -> {
                    CategoryModel newCategory = new CategoryModel();
                    newCategory.setCategoryName(category.textContent().trim());
                    newCategory.setCategoryUrl(category.getAttribute("href"));
                    categoryList.add(newCategory);
                });
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
