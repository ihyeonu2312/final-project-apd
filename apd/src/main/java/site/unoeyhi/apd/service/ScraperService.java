// package site.unoeyhi.apd.service;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import com.microsoft.playwright.*;

// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;



// public class ScraperService {

//     public void ScrapAliExpress() {
//         List<CategoryModel> categoryList = new ArrayList<>(); // 결과 담을 리스트

//         // ✅ Playwright 실행 및 페이지 초기화
//         try (Playwright playwright = Playwright.create()) {
//             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
//             Page page = (Page) browser; // `page` 변수를 선언하고 초기화

//             page.navigate("https://www.aliexpress.com/");
//             page.waitForTimeout(2500); // 이동 후 2.5초 대기

//             /// 1. 팝업 제거
//             closePopups(page);

//             /// 2. 카테고리 호버
//             hoverOverCategories(page);

//             /// 3. 카테고리 목록 스크랩
//             categoryList = scrapeCategories(page);

//             System.out.println(categoryList);
//             browser.close(); // 브라우저 종료
//         }
//     }

//     private void closePopups(Page page) {
//         Locator popupClose1 = page.locator("img.pop-close-btn");
//         Locator popupClose2 = page.locator("body > div:nth-child(31) > div > img");
//         Locator popupClose3 = page.locator("body > div:nth-child(30) > div > img");
//         Locator popupClose4 = page.locator("body > div:nth-child(32) > div > img");

//         if (popupClose1.count() > 0) popupClose1.click();
//         page.waitForTimeout(500);
//         if (popupClose2.count() > 0) popupClose2.click();
//         if (popupClose3.count() > 0) popupClose3.click();
//         if (popupClose4.count() > 0) popupClose4.click();
//         page.waitForTimeout(500);
//     }

//     private void hoverOverCategories(Page page) {
//         Locator hoverCategory = page.locator("div[data-spm=allcategoriespc]");
//         if (hoverCategory.count() > 0) {
//             hoverCategory.hover();
//             page.waitForTimeout(1000);
//         }
//     }

//     private List<CategoryModel> scrapeCategories(Page page) {
//         List<CategoryModel> categoryList = new ArrayList<>();
//         Locator categories = page.locator("div.at_aw > div > div > div:nth-child(1) > div > ul > a");

//         categories.waitFor(); // 요소가 나타날 때까지 대기

//         if (categories.count() > 0) {
//             categories.all().forEach(category -> {
//                 String name = Optional.ofNullable(category.textContent()).orElse("Unknown");
//                 String url = Optional.ofNullable(category.getAttribute("href")).orElse("#");

//                 categoryList.add(new CategoryModel(name, url));
//             });
//         }
//         return categoryList;
//     }

//     public static void main(String[] args) {
//         ScraperService scraper = new ScraperService();
//         scraper.ScrapAliExpress();
//     }
// }
