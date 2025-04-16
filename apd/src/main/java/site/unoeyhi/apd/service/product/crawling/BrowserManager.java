package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;


import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// @ConditionalOnProperty(name = "crawler.enabled", havingValue = "true") //브라우저 비활성화
// @Service // 로컬에선 해제
public class BrowserManager {
    private static final Path COOKIE_PATH = Paths.get("cookies.json");

    private static Playwright playwright;
    private static Browser browser;

    /**
     * ✅ 싱글턴 패턴 적용된 BrowserManager 생성자
     */
    public BrowserManager() {
        initializePlaywright();
    }

    /**
     * ✅ Playwright 초기화 (싱글턴 유지, 멀티스레드 안전)
     */
    private synchronized void initializePlaywright() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setExecutablePath(Paths.get("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")) // ✅ Path 타입 변환
                .setHeadless(false) // ✅ 헤드리스 모드 비활성화
                .setArgs(List.of(
                    "--disable-http2", // ✅ HTTP2 비활성화
                    "--disable-blink-features=AutomationControlled", // ✅ 봇 탐지 우회
                    "--disable-features=NetworkService", // ✅ 추가: 네트워크 서비스 비활성화
                    "--disable-features=ChromeWhatsNewUI" // ✅ 추가: 불필요한 기능 차단
                ))
            );
            System.out.println("🛠 [디버그] Playwright Chrome 버전: " + browser.version());
        }
    }
    

    /**
     * ✅ 쿠키 기반으로 브라우저 컨텍스트 생성
     */
    public synchronized BrowserContext createOrLoadContext() {
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
    
        // ✅ `setExtraHTTPHeaders()`를 Context 생성 시점에서 추가!
        BrowserContext context = browser.newContext(contextOptions);
    
        if (Files.exists(COOKIE_PATH)) {
            System.out.println("🍪 [쿠키 로드] 기존 쿠키 파일 존재, 불러오기 시도...");
            context.storageState(new BrowserContext.StorageStateOptions().setPath(COOKIE_PATH));
    
            if (isUserLoggedIn(context)) {
                System.out.println("✅ [쿠키 로그인 성공] 기존 쿠키 사용.");
                return context;
            } else {
                System.out.println("🚨 [쿠키 만료] 다시 로그인 필요.");
            }
        } else {
            System.out.println("🚨 [쿠키 없음] 로그인 필요.");
        }
    
        context = loginAndSaveCookies(browser);
        return context;
    }
    

    /**
     * ✅ 로그인 후 쿠키 저장 (BrowserContext 인자를 받도록 수정)
     */
    private BrowserContext loginAndSaveCookies(Browser browser) {
        BrowserContext context = browser.newContext(); // ✅ 새로운 컨텍스트 생성
        Page page = context.newPage();
        page.navigate("https://login.coupang.com/", new Page.NavigateOptions().setTimeout(60000));

        System.out.println("🛑 [로그인 필요] 브라우저에서 로그인 후 엔터를 입력하세요...");
        new Scanner(System.in).nextLine();

        saveCookies(context);
        return context;
    }



    /**
     * ✅ 사용자 로그인 상태 확인
     */
    private boolean isUserLoggedIn(BrowserContext context) {
        return context.cookies().stream().anyMatch(cookie -> "sid".equals(cookie.name));
    }

    /**
     * ✅ 로그인 후 쿠키 저장
     */
    private BrowserContext loginAndSaveCookies(BrowserContext context) {
        Page page = context.newPage();
        page.navigate("https://login.coupang.com/", new Page.NavigateOptions().setTimeout(60000));

        System.out.println("🛑 [로그인 필요] 브라우저에서 로그인 후 엔터를 입력하세요...");
        new Scanner(System.in).nextLine();

        saveCookies(context);
        return context;
    }

    /**
     * ✅ 쿠키 저장
     */
    private void saveCookies(BrowserContext context) {
        try {
            context.storageState(new BrowserContext.StorageStateOptions().setPath(COOKIE_PATH));
            System.out.println("✅ [쿠키 저장 완료] " + COOKIE_PATH.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("🚨 [쿠키 저장 실패] " + e.getMessage());
        }
    }

    /**
     * ✅ 브라우저 종료 (테스트 종료 시 필요)
     */
    public synchronized void closeBrowser() {
        try {
            if (browser != null) {
                browser.close();
                browser = null;
                System.out.println("✅ [브라우저 종료]");
            }
            if (playwright != null) {
                playwright.close();
                playwright = null;
                System.out.println("✅ [Playwright 종료]");
            }
        } catch (Exception e) {
            System.out.println("🚨 [브라우저 종료 중 오류 발생] " + e.getMessage());
        }
    }
}