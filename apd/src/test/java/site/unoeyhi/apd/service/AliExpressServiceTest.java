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
                .setArgs(List.of("--disable-blink-features=AutomationControlled"))); // ✅ 탐지 방지

        context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36") // ✅ 탐지 방지
                .setViewportSize(1366, 768)); // 화면 해상도 설정

        page = context.newPage();
    }


    @Test
    void ScrapAliExpress() {
        List<CategoryModel> categoryList = new ArrayList<>(); //결과 담을 모델 리스트

        page.navigate("https://www.aliexpress.com/");
        page.waitForTimeout(2500); // 이동후 2초 대기

            System.out.println("✅ 페이지 이동 완료");

            /// 1. 팝업 제거
            System.out.println("🔵 팝업 제거 시작");
            try {
                Locator popupClose1 = page.locator("img.pop-close-btn");
                Locator popupClose2 = page.locator("body > div:nth-child(31) > div > img");
                Locator popupClose3 = page.locator("body > div:nth-child(30) > div > img");
                Locator popupClose4 = page.locator("body > div:nth-child(32) > div > img");

                if (popupClose1.count() > 0) {
                    popupClose1.click();
                    System.out.println("✅ 팝업 1 제거 완료");
                }
                page.waitForTimeout(500);

                if (popupClose2.count() > 0) {
                    popupClose2.click();
                    System.out.println("✅ 팝업 2 제거 완료");
                }
                if (popupClose3.count() > 0) {
                    popupClose3.click();
                    System.out.println("✅ 팝업 3 제거 완료");
                }
                if (popupClose4.count() > 0) {
                    popupClose4.click();
                    System.out.println("✅ 팝업 4 제거 완료");
                }
                page.waitForTimeout(500);
            } catch (Exception e) {
                System.out.println("⚠️ 팝업 제거 중 오류 발생: " + e.getMessage());
            }

            /// 2. 카테고리 호버
            System.out.println("🔵 카테고리 호버 시작");
            try {
                page.waitForSelector("div[data-spm=allcategoriespc]", new Page.WaitForSelectorOptions().setTimeout(5000)); // 5초 대기
                Locator hoverCategory = page.locator("div[data-spm=allcategoriespc]");
                if (hoverCategory.count() > 0) {
                    hoverCategory.hover();
                    System.out.println("✅ 카테고리 호버 완료");
                } else {
                    System.out.println("⚠️ 카테고리 요소를 찾을 수 없음");
                }
            } catch (Exception e) {
                System.out.println("⚠️ 카테고리 호버 중 오류 발생: " + e.getMessage());
            }
            page.waitForTimeout(1000);

            /// 3. 스크랩 실행
            System.out.println("🔵 카테고리 스크랩 시작");
            try {
                Locator categories = page.locator("ul.Categoey--categoryList--2QES_k6 > a");
                if (categories.count() > 0) {
                    categories.all().forEach(category -> {
                        CategoryModel newCategory = new CategoryModel();

                        newCategory.setCategoryName(category.textContent()); // 카테고리명
                        newCategory.setCategoryUrl(category.getAttribute("href")); // URL

                        categoryList.add(newCategory);
                    });
                    System.out.println("✅ 카테고리 스크랩 완료");
                } else {
                    System.out.println("⚠️ 카테고리 목록을 찾을 수 없음");
                }
            } catch (Exception e) {
                System.out.println("⚠️ 스크랩 중 오류 발생: " + e.getMessage());
            }

            System.out.println("📌 최종 결과: " + categoryList);
            browser.close();
   }
}
