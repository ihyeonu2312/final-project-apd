// package site.unoeyhi.apd.service.product.crawling;

// import com.microsoft.playwright.*;
// import com.microsoft.playwright.options.WaitUntilState;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;
// import site.unoeyhi.apd.entity.Category;
// import site.unoeyhi.apd.repository.CategoryRepository;
// import org.springframework.context.annotation.Lazy;

// import java.nio.file.Paths;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;

// @Service
// public class CategoryCrawler {
//     private final CategoryRepository categoryRepository;
//     private final ProductCrawler productCrawler;
//     private final ExecutorService executorService = Executors.newFixedThreadPool(3); // ✅ 동시 실행 개수 제한

//     @Autowired
//     public CategoryCrawler(CategoryRepository categoryRepository, @Lazy ProductCrawler productCrawler) {
//         this.categoryRepository = categoryRepository;
//         this.productCrawler = productCrawler;
//     }

//     private static final int MAX_CATEGORY_CRAWL = 15; // ✅ 최대 크롤링할 카테고리 개수

//     @Async
//     public CompletableFuture<Void> crawlAllCategories() {
//         System.out.println("🚀 [크롤링 시작] 모든 카테고리 크롤링");
    
//         List<Category> categories = categoryRepository.findAll();
    
//         if (categories.isEmpty()) {
//             System.out.println("🚨 [크롤링 중단] 크롤링할 카테고리가 없습니다!");
//             return CompletableFuture.completedFuture(null);
//         }
    
//         // ✅ 최대 MAX_CATEGORY_CRAWL 개수까지만 크롤링
//         int limit = Math.min(categories.size(), MAX_CATEGORY_CRAWL);
//         List<Category> selectedCategories = categories.subList(0, limit);
    
//         List<CompletableFuture<Void>> futures = new ArrayList<>();
    
//         try (Playwright playwright = Playwright.create()) {
//             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
//                     .setExecutablePath(Paths.get("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")) // ✅ 크롬 직접 지정
//                     .setHeadless(false));
    
//             for (Category category : selectedCategories) { // ✅ 선택된 카테고리만 크롤링
//                 CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                     BrowserContext context = browser.newContext(); // ✅ BrowserContext 생성
    
//                     // ✅ 각 카테고리에서 상품 n개씩만 가져오도록 설정
//                     productCrawler.crawlAllProducts(context, "https://www.coupang.com" + category.getUrl(), 60,category.getCategoryId());
    
//                     context.close(); // ✅ 크롤링 후 context 닫기 (메모리 관리)
//                 }, executorService);
//                 futures.add(future);
//             }
    
//             return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                     .thenRun(() -> {
//                         executorService.shutdown();
//                         if (browser != null) {
//                             System.out.println("🛑 [브라우저 종료] 크롤링 완료 후 브라우저 닫음");
//                             browser.close();
//                         }
//                     });
//         } catch (Exception e) {
//             System.out.println("🚨 [오류 발생] " + e.getMessage());
//             return CompletableFuture.completedFuture(null);
//         }
//     }
    



//     private void crawlProductsByCategory(Browser browser, Category category) {
//         System.out.println("🚀 [crawlProductsByCategory] 카테고리 크롤링 시작: " + category.getCategoryName());
    
//         if (browser == null || category == null) {
//             System.out.println("🚨 [오류] 브라우저 또는 카테고리가 null입니다.");
//             return;
//         }
    
//         // ✅ 새로운 BrowserContext 생성 (User-Agent 변경, HTTP/1.1 강제)
//         BrowserContext context = browser.newContext(new Browser.NewContextOptions()
//             .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.6998.36 Safari/537.36")
//             .setExtraHTTPHeaders(Map.of(
//                 "Upgrade-Insecure-Requests", "1",  // ✅ HTTP/1.1 강제 사용
//                 "Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
//                 "Referer", "https://www.coupang.com/",
//                 "Connection", "keep-alive",  // ✅ 지속적인 연결 유지
//                 "Cache-Control", "no-cache" // ✅ 캐시 방지
//             ))
//         );
    
//         if (context == null) {
//             System.out.println("🚨 [오류] `context` 초기화 실패, 크롤링 중단.");
//             return;
//         }
    
//         String categoryUrl = "https://www.coupang.com" + category.getUrl();
//         Page page = context.newPage();
        
//         int retryCount = 0;
//         boolean success = false;
    
//         while (retryCount < 3 && !success) {
//             try {
//                 System.out.println("🔄 [카테고리 페이지 이동 시도] " + categoryUrl + " (재시도 " + (retryCount + 1) + "회)");
//                 page.navigate(categoryUrl, new Page.NavigateOptions()
//                     .setTimeout(60000)
//                     .setWaitUntil(WaitUntilState.LOAD)  // ✅ 페이지 완전히 로드될 때까지 대기
//                 );
    
//                 if (!page.url().equals("about:blank") && !page.title().isEmpty()) {
//                     success = true; // ✅ 페이지가 정상 로드되면 종료
//                     System.out.println("✅ [카테고리 페이지 로딩 성공]: " + page.title());
//                 }
//             } catch (Exception e) {
//                 System.out.println("🚨 [페이지 이동 실패]: " + e.getMessage());
//                 retryCount++;
//                 page.reload(); // ✅ 실패 시 페이지 새로고침 후 다시 시도
//             }
//         }
    
//         if (!success) {
//             System.out.println("🚨 [최종 실패] 카테고리 페이지 로드 불가: " + categoryUrl);
//             return;
//         }
    
//         // ✅ 상품 목록 로딩 대기 (JavaScript 로딩 대응)
//         page.waitForTimeout(3000);
//         page.waitForSelector("li.baby-product.renew-badge", new Page.WaitForSelectorOptions().setTimeout(10000));
    
//         List<ElementHandle> productElements = page.querySelectorAll("li.baby-product.renew-badge");
//         if (productElements.isEmpty()) {
//             System.out.println("🚨 [경고] 상품 없음! 크롤링 중단.");
//             return;
//         }
    
//         System.out.println("📦 [총 상품 개수] " + productElements.size());
    
//         for (ElementHandle productElement : productElements) {
//             ElementHandle linkElement = productElement.querySelector("a.baby-product-link");
//             if (linkElement == null || linkElement.getAttribute("href") == null) continue;
    
//             String detailUrl = "https://www.coupang.com" + linkElement.getAttribute("href");
//             System.out.println("🔗 [상품 URL] " + detailUrl);
    
//             System.out.println("🛠 [crawlProductsByCategory] 상품 상세 크롤링 호출: " + detailUrl);
//             productCrawler.crawlProductDetail(context, detailUrl,category.getCategoryId());
//         }
    
//         page.close();
//         context.close(); // ✅ `context` 닫기 (메모리 누수 방지)
//     }
// }
