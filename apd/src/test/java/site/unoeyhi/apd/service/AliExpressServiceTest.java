// package site.unoeyhi.apd.service;

// import org.springframework.boot.test.context.SpringBootTest;

// import com.microsoft.playwright.options.LoadState;

// import site.unoeyhi.apd.model.CategoryModel;


// import com.microsoft.playwright.*;

// import org.junit.jupiter.api.*;

// import java.util.*;

// @SpringBootTest
// public class AliExpressServiceTest {
//     private Playwright playwright;
//     private Browser browser;
//     private BrowserContext context;
//     private Page page;

//     @BeforeEach
//     void setUp() {
//         playwright = Playwright.create();
//         browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

//         // User-Agent 설정된 BrowserContext 생성
//         context = browser.newContext(new Browser.NewContextOptions().setUserAgent(
//             "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
//         ));

//         page = context.newPage(); // User-Agent가 적용된 페이지 생성

//         // 추가 HTTP 헤더 설정
//         page.setExtraHTTPHeaders(Map.of(
//             "Accept-Language", "en-US,en;q=0.9",
//             "Referer", "https://www.google.com"
//         ));

//         // Playwright 자동화 탐지 우회
//         page.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => undefined })");

//         // Viewport 설정
//         page.setViewportSize(1280, 800);
//     }

//     @AfterEach
//     void tearDown() {
//         browser.close();
//         playwright.close();
//     }

//     @Test
//     void ScrapAliExpress() {
//         try {
//             // 페이지 이동 (타임아웃 5분으로 설정)
//             page.navigate("https://www.aliexpress.com/", new Page.NavigateOptions().setTimeout(300000));
//             page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//             page.waitForTimeout(5000); // 페이지 안정화 대기

//             // ✅ `page.waitForFunction()` 올바르게 사용
//             page.waitForFunction("() => document.querySelector('.category-list') !== null"); 

//             // ✅ `page.waitForSelector()` 타임아웃을 60초로 늘림
//             page.waitForSelector(".category-list", new Page.WaitForSelectorOptions().setTimeout(60000));

            
//             System.out.println("페이지 이동 완료");

//             // 팝업 닫기
//             List<String> popupSelectors = List.of(
//                 "img.pop-close-btn",
//                 "body > div:nth-child(31) > div > img",
//                 "body > div:nth-child(30) > div > img",
//                 "body > div:nth-child(32) > div > img"
//             );

//             for (String selector : popupSelectors) {
//                 Locator popup = page.locator(selector);
//                 if (popup.count() > 0) {
//                     popup.click();
//                     page.waitForTimeout(500);
//                 }
//             }

//             // 카테고리 정보 크롤링
//             List<CategoryModel> categoryList = new ArrayList<>();
//             Locator categories = page.locator("div.at_aw > div > div > div:nth-child(1) > div > ul > a");

//             if (categories.count() > 0) {
//                 categories.all().forEach(category -> {
//                     CategoryModel newCategory = new CategoryModel();
//                     newCategory.setCategoryName(category.textContent());
//                     newCategory.setCategoryUrl(category.getAttribute("href"));
//                     categoryList.add(newCategory);
//                 });
//             }

//             System.out.println("카테고리 리스트: " + categoryList);
//         } catch (Exception e) {
//             System.err.println("❌ 크롤링 중 오류 발생: " + e.getMessage());
//         }
//     }
// }

    