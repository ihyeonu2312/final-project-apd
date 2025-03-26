// package site.unoeyhi.apd.service.product.crawling;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.concurrent.ThreadLocalRandom;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.microsoft.playwright.BrowserContext;
// import com.microsoft.playwright.Locator;
// import com.microsoft.playwright.Page;
// import com.microsoft.playwright.options.WaitUntilState;

// import site.unoeyhi.apd.service.product.ProductDetailImageService;

// @Service
// public class ProductDetailImageCrawler {

//     @Autowired
//     private ProductDetailImageService productDetailImageService;

//     /** ✅ 상세 이미지 크롤링 & DB 저장 */
//     public void crawlAndSaveDetailImages(BrowserContext context, Long productId, String detailUrl) {
//         System.out.println("🚀 [크롤링 시작] 상품 ID: " + productId + " | URL: " + detailUrl);

//         Page detailPage = context.newPage();
//         detailPage.navigate(detailUrl, new Page.NavigateOptions()
//                 .setTimeout(40000)  // 기존 60초 → 40초로 단축
//                 .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

//         // ✅ 랜덤 딜레이 (300ms ~ 1.5초)
//         detailPage.waitForTimeout(ThreadLocalRandom.current().nextInt(300, 1500));

//         // ✅ 상세 이미지 URL 추출
//         List<String> imageUrls = extractDetailImages(detailPage);

//         // ✅ 상세 이미지가 없는 경우 기본 이미지 설정
//         if (imageUrls.isEmpty()) {
//             System.out.println("⚠️ [경고] 상세 이미지 없음! 기본 이미지 추가: " + detailUrl);
//             imageUrls.add("https://via.placeholder.com/500?text=No+Image");
//         }

//         System.out.println("📸 [크롤링 성공] 상품 ID: " + productId + " | 상세 이미지 개수: " + imageUrls.size());

//         // ✅ 상세 이미지 DB 저장
//         productDetailImageService.saveDetailImages(productId, imageUrls);

//         detailPage.close();

//         // ✅ 상품 간 랜덤 대기 시간 추가 (1초 ~ 3초)
//         try {
//             Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }
//     }

//     /** ✅ 상세 이미지 추출 */
//     public List<String> extractDetailImages(Page detailPage) {
//         List<String> images = new ArrayList<>();

//         // ✅ 상세 이미지 요소 로딩 대기 (기존 20초 → 10초)
//         detailPage.waitForSelector("div.product-detail-content-inside img", new Page.WaitForSelectorOptions().setTimeout(10000));

//         // ✅ 스크롤 최적화 (기존 3~7회 → 2~5회)
//         randomScroll(detailPage);

//         // ✅ `product-detail-content` 내 모든 이미지 가져오기
//         List<Locator> imgLocators = detailPage.locator("div.product-detail-content-inside img").all();

//         for (Locator imgLocator : imgLocators) {
//             imgLocator.scrollIntoViewIfNeeded(); // ✅ 이미지가 보이도록 스크롤

//             String imgSrc = imgLocator.getAttribute("src");
//             if (imgSrc == null || imgSrc.trim().isEmpty()) {
//                 imgSrc = imgLocator.getAttribute("data-src");  // ✅ `data-src`도 체크
//             }

//             if (imgSrc != null && !imgSrc.trim().isEmpty()) {
//                 images.add(imgSrc);
//                 System.out.println("✅ [상세 이미지 발견] " + imgSrc);
//             }
//         }

//         System.out.println("📸 [상세 이미지 크롤링 완료] 총 " + images.size() + "개 발견");
//         return images;
//     }

//     /** ✅ 랜덤 스크롤 (최적화) */
//     private void randomScroll(Page page) {
//         int scrollTimes = ThreadLocalRandom.current().nextInt(2, 5); // ✅ 2~5회
//         int scrollDelay = ThreadLocalRandom.current().nextInt(500, 1200); // ✅ 기존 500~1500ms → 500~1200ms

//         for (int i = 0; i < scrollTimes; i++) {
//             boolean scrollUp = Math.random() < 0.3; // 30% 확률로 위로 스크롤
//             int scrollAmount = ThreadLocalRandom.current().nextInt(300, 600); // ✅ 기존 300~700px → 300~600px로 최적화

//             try {
//                 if (scrollUp) {
//                     page.evaluate("window.scrollBy(0, -" + scrollAmount + ")");
//                     System.out.println("📜 [스크롤] 위로 " + scrollAmount + "px 이동");
//                 } else {
//                     page.evaluate("window.scrollBy(0, " + scrollAmount + ")");
//                     System.out.println("📜 [스크롤] 아래로 " + scrollAmount + "px 이동");
//                 }
//                 page.waitForTimeout(scrollDelay);
//             } catch (Exception e) {
//                 System.out.println("🚨 [스크롤 오류] " + e.getMessage());
//             }
//         }
//     }
// }
