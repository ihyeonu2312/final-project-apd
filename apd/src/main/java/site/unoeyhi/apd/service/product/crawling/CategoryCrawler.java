package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CategoryCrawler {
    private final CategoryRepository categoryRepository;
    private final BrowserManager browserManager;
    private final ProductCrawler productCrawler;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3); // ✅ 동시 실행 개수 제한

    @Autowired
    public CategoryCrawler(CategoryRepository categoryRepository, BrowserManager browserManager, ProductCrawler productCrawler) {
        this.categoryRepository = categoryRepository;
        this.browserManager = browserManager;
        this.productCrawler = productCrawler;
    }

    @Async
    public CompletableFuture<Void> crawlAllCategories() {
        System.out.println("🚀 [크롤링 시작] 모든 카테고리 크롤링");

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            System.out.println("🚨 [크롤링 중단] 크롤링할 카테고리가 없습니다!");
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) { // ✅ Playwright 하나만 생성
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

            for (Category category : categories) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> crawlProductsByCategory(browser, category), executorService);
                futures.add(future);
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .whenComplete((result, ex) -> {
                        executorService.shutdown(); // ✅ 스레드 종료
                        if (browser != null) {
                            browser.close(); // ✅ `browser`를 안전하게 닫음
                        }
                    });
        } catch (Exception e) {
            System.out.println("🚨 [오류 발생] " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    private synchronized void crawlProductsByCategory(Browser browser, Category category) { // ✅ `synchronized` 추가
        System.out.println("🚀 [crawlProductsByCategory] 카테고리 크롤링 시작: " + category.getCategoryName()); // ✅ 로그 추가
        if (browser == null || category == null) {
            System.out.println("🚨 [오류] 브라우저 또는 카테고리가 null입니다.");
            return;
        }

        BrowserContext context = browserManager.createOrLoadContext(); // ✅ Browser 전달
        if (context == null) {
            System.out.println("🚨 [오류] `context` 초기화 실패, 크롤링 중단.");
            return;
        }

        String categoryUrl = "https://www.coupang.com" + category.getUrl();
        Page page = context.newPage();
        page.navigate(categoryUrl, new Page.NavigateOptions().setTimeout(60000).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        if (page.url().equals("about:blank") || page.title().isEmpty()) {
            System.out.println("🚨 [경고] 페이지 로드 실패! 크롤링 중단");
            return;
        }

        List<ElementHandle> productElements = page.querySelectorAll("li.baby-product.renew-badge");
        if (productElements.isEmpty()) {
            System.out.println("🚨 [경고] 상품 없음! 크롤링 중단.");
            return;
        }

        System.out.println("📦 [총 상품 개수] " + productElements.size());

        for (ElementHandle productElement : productElements) {
            ElementHandle linkElement = productElement.querySelector("a.baby-product-link");
            if (linkElement == null || linkElement.getAttribute("href") == null) continue;

            String detailUrl = "https://www.coupang.com" + linkElement.getAttribute("href");
            System.out.println("🔗 [상품 URL] " + detailUrl);

            System.out.println("🛠 [crawlProductsByCategory] 상품 상세 크롤링 호출: " + detailUrl);
            productCrawler.crawlProductDetail(context, detailUrl);
        }

        page.close();
    }
}
