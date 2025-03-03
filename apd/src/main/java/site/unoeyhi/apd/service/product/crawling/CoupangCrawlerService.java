package site.unoeyhi.apd.service.product.crawling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Service
@EnableAsync
public class CoupangCrawlerService {

    private final CategoryCrawler categoryCrawler;
    private final CoupangLogin coupangLogin; // ✅ 로그인 기능 추가

    @Autowired
    public CoupangCrawlerService(CategoryCrawler categoryCrawler, CoupangLogin coupangLogin) {
        this.categoryCrawler = categoryCrawler;
        this.coupangLogin = coupangLogin;
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

        // ✅ 3. 카테고리 크롤링 실행
        return categoryCrawler.crawlAllCategories();
    }
}
