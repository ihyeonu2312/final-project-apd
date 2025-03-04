package site.unoeyhi.apd.service.product.crawling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.service.product.ProductService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@EnableAsync
public class CoupangCrawlerService {

    private final CategoryCrawler categoryCrawler;
    private final CoupangLogin coupangLogin;
    private final ProductCrawler productCrawler; // ✅ 상품 크롤러 추가
    private final ProductService productService; // ✅ 상품 저장 서비스 추가
    private final CategoryRepository categoryRepository;
    @Autowired
    public CoupangCrawlerService(CategoryCrawler categoryCrawler, CoupangLogin coupangLogin,
                                 ProductCrawler productCrawler, ProductService productService,
                                 CategoryRepository categoryRepository) {
        this.categoryCrawler = categoryCrawler;
        this.coupangLogin = coupangLogin;
        this.productCrawler = productCrawler;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    /**
     * ✅ Playwright 브라우저 컨텍스트 생성 메서드
     */
    private BrowserContext createBrowserContext() {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(false) // ✅ 헤드리스 모드 해제
            .setChannel("chrome") // Playwright이 크롬 채널을 사용하도록 설정
            .setExecutablePath(Paths.get("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")) // Chrome 실행 파일 경로

            .setArgs(List.of(
                "--disable-blink-features=AutomationControlled", // ✅ Playwright 자동화 탐지 우회
                "--disable-http2",  // ✅ HTTP/2 비활성화 (강제 HTTP/1.1 사용)
                "--disable-features=NetworkService,NetworkServiceInProcess" // ✅ 네트워크 감지 우회
            ))
         );
         // ✅ HTTP/2 대신 HTTP/1.1 사용
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
        .setExtraHTTPHeaders(Map.of(
            "Upgrade-Insecure-Requests", "1",  // ✅ HTTP/1.1 강제 사용
            "Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
            "Referer", "https://www.coupang.com/",
            "Connection", "keep-alive",  // ✅ 지속적인 연결 유지
            "Cache-Control", "no-cache",  // ✅ 캐시 방지
            "DNT", "1"  // ✅ 추적 방지
        ))
        .setBypassCSP(true);  // ✅ 콘텐츠 보안 정책 우회
    
        return browser.newContext(contextOptions);
    }

    /**
     * ✅ 전체 크롤링 실행 (로그인 → 카테고리 → 상품)
     */
    public CompletableFuture<Void> startCrawling() {
        System.out.println("🚀 [크롤링 시작] 로그인 & 카테고리 크롤링 진행");

        // ✅ 1. 로그인 수행 & 쿠키 저장
        coupangLogin.loginAndSaveCookies();

        // ✅ 2. 쿠키 파일이 정상적으로 생성되었는지 확인
        if (!Files.exists(Paths.get("cookies.json"))) {
            System.out.println("🚨 [오류] 로그인 후 쿠키 파일이 생성되지 않음. 크롤링 중단.");
            return CompletableFuture.completedFuture(null);
        }

        // ✅ 3. 카테고리 크롤링 실행 후 상품 크롤링 & 저장 실행
        return crawlAllCategories()
        .thenComposeAsync(optionalCategoryUrls -> CompletableFuture.runAsync(() -> {
            BrowserContext context = createBrowserContext();  // ✅ Playwright 브라우저 컨텍스트 생성
            
            // ✅ Optional에서 값 꺼내기 (값이 없으면 빈 리스트 반환)
            List<String> categoryList = optionalCategoryUrls.orElse(List.of());

            for (String categoryUrl : categoryList) {
                System.out.println("🔗 [CoupangCrawler] 크롤링할 카테고리: " + categoryUrl);
                crawlAndSaveProducts(context, categoryUrl);
            }
        }));
    }

    /**
     * ✅ 쿠팡 카테고리 크롤링 (전체 카테고리 URL 가져오기)
     */
    public CompletableFuture<Optional<List<String>>> crawlAllCategories() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("🚀 [크롤링 시작] 모든 카테고리 크롤링");
    
            // ✅ 기존 DB에서 카테고리 조회crawlProductsByCategory
            List<Category> categories = categoryRepository.findAll();
    
            if (categories.isEmpty()) {
                System.out.println("🚨 [크롤링 중단] 크롤링할 카테고리가 없습니다!");
                return Optional.empty();
            }
    
            List<String> categoryUrls = new ArrayList<>();
            for (Category category : categories) {
                String categoryUrl = "https://www.coupang.com" + category.getUrl();
                categoryUrls.add(categoryUrl);
                System.out.println("🔗 [CoupangCrawler] 카테고리 URL 발견: " + categoryUrl);
            }
    
            System.out.println("📦 [CoupangCrawler] 최종 크롤링된 카테고리 개수: " + categoryUrls.size());
            return Optional.of(categoryUrls);
        });
    }
    
    

    /**
     * ✅ 상품 크롤링 후 자동 저장
     */
    private void crawlAndSaveProducts(BrowserContext context, String categoryUrl) {
        System.out.println("🚀 [CoupangCrawler] 카테고리 상품 크롤링 시작: " + categoryUrl);

        List<ProductDto> products = productCrawler.crawlAllProducts(context, categoryUrl); // ✅ 카테고리 전체 상품 크롤링 실행

        if (products.isEmpty()) {
            System.out.println("🚨 [CoupangCrawler] 크롤링된 상품 없음! 저장 중단.");
            return;
        }

        for (ProductDto productDto : products) {
            System.out.println("📦 [CoupangCrawler] 상품 저장 시도: " + productDto.getName());
            productService.saveProduct(productDto);  // ✅ 크롤링된 상품을 DB에 저장
        }

        System.out.println("✅ [CoupangCrawler] 상품 저장 완료!");
    }
}
