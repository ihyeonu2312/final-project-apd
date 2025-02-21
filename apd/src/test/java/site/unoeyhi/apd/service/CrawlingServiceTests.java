package site.unoeyhi.apd.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import lombok.extern.log4j.Log4j2;
import site.unoeyhi.apd.service.product.crawling.CrawlingService;

@SpringBootTest
@Log4j2
public class CrawlingServiceTests {
  @Autowired
  private CrawlingService service;

  @Test
  public void crawlingTest(){
    service.crawling();
  }


//   @Test
//   public void testCategorySelector() {
//     try (Playwright playwright = Playwright.create()) {
//         Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
//             .setHeadless(false)  // 헤드리스 모드 비활성화 (브라우저 띄우기)
//         );

//         BrowserContext context = browser.newContext();
//         Page page = context.newPage();

//         // ✅ 쿠팡 메인 페이지 이동
//         page.navigate("https://www.coupang.com");
//         page.waitForLoadState(LoadState.NETWORKIDLE);

//         // ✅ 대분류 카테고리 선택자 테스트
//         List<ElementHandle> categoryElements = page.querySelectorAll("ul.gnb-depth1 > li > a");

//         if (categoryElements.isEmpty()) {
//             log.warn("❌ 대분류 카테고리를 찾을 수 없습니다.");
//         } else {
//             log.info("✅ 대분류 카테고리 개수: " + categoryElements.size());
//             for (ElementHandle element : categoryElements) {
//                 log.info("🔹 " + element.textContent() + " - " + element.getAttribute("href"));
//             }
//         }
//     }
//   }
 }

