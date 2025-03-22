package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import site.unoeyhi.apd.service.product.ProductDetailImageService;
import site.unoeyhi.apd.service.product.crawling.ProductDetailImageCrawler;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Rollback(false)
public class ProductDetailImageCrawlerTests {

    @Autowired
    private ProductDetailImageService productDetailImageService; 

    @InjectMocks
    private ProductDetailImageCrawler productDetailImageCrawler;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;

    /**
     * ✅ Playwright 초기화
     */
    private synchronized void initializePlaywright() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setExecutablePath(Paths.get("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")) // ✅ 크롬 실행 경로
                .setHeadless(false) // ✅ 브라우저 창을 띄우도록 설정
                .setSlowMo(500) // ✅ 동작을 500ms씩 천천히 수행 (디버깅 용이)
                .setArgs(List.of(
                    "--disable-http2", // ✅ HTTP2 비활성화
                    "--disable-blink-features=AutomationControlled", // ✅ 봇 탐지 우회
                    "--disable-features=NetworkService", // ✅ 네트워크 서비스 비활성화
                    "--disable-features=ChromeWhatsNewUI" // ✅ 불필요한 기능 차단
                ))
            );
            System.out.println("🛠 [디버그] Playwright Chrome 버전: " + browser.version());
        }
    }
    

    /**
     * ✅ 쿠키 기반으로 브라우저 컨텍스트 생성
     */
    private synchronized BrowserContext createOrLoadContext() {
        if (browser == null) {
            System.out.println("🚨 [오류] `browser`가 null입니다. `BrowserContext`를 생성할 수 없습니다.");
            return null;
        }

        String detectedChromeVersion = browser.version();
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"
                + detectedChromeVersion + " Safari/537.36";

        System.out.println("🛠 [디버그] 설정된 User-Agent: " + userAgent);

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
            .setUserAgent(userAgent)
            .setBypassCSP(true)
            .setExtraHTTPHeaders(Map.of(
                "Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
                "Connection", "keep-alive",
                "Referer", "https://www.coupang.com/"
            ));

        return browser.newContext(contextOptions); // ✅ `context` 생성 및 반환
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initializePlaywright(); // ✅ Playwright 초기화
        context = createOrLoadContext(); // ✅ `context` 생성
    }

    @Test
    void testExtractAllDetailImages() {
    List<Map<String, Object>> productDetails = productDetailImageService.findAllProductDetails(); // ✅ 전체 상품 가져오기

        if (productDetails.isEmpty()) {
            System.out.println("🚨 [오류] DB에 상품이 없습니다!");
            return;
        }

        for (Map<String, Object> product : productDetails) {
            Long productId = (Long) product.get("productId");

            if (productId < 188) continue; // ✅✅✅✅✅✅✅✅이어서 하기✅✅✅✅✅✅✅✅✅

            String detailUrl = (String) product.get("detailUrl");

            if (detailUrl == null || detailUrl.isEmpty()) {
                System.out.println("⚠️ [경고] 상품 ID " + productId + "의 상세 URL이 없음. 건너뜀.");
                continue;
            }

            System.out.println("🚀 [크롤링 시작] 상품 ID: " + productId + " | URL: " + detailUrl);

            Page detailPage = context.newPage();
            detailPage.navigate(detailUrl, new Page.NavigateOptions().setTimeout(90000).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            // ✅ 상세 이미지 크롤링 실행
            List<String> imageUrls = productDetailImageCrawler.extractDetailImages(detailPage);

            if (imageUrls.isEmpty()) {
                System.out.println("⚠️ [경고] 상품 ID " + productId + "의 상세 이미지 없음!");
            } else {
                System.out.println("📸 [크롤링 완료] 상품 ID: " + productId + " | 크롤링된 이미지 개수: " + imageUrls.size());
                imageUrls.forEach(url -> System.out.println("🔗 이미지 URL: " + url));

                // ✅ 크롤링된 상세 이미지 DB에 저장
                productDetailImageService.saveDetailImages(productId, imageUrls);
            }

            detailPage.close();
        }
    }
}
