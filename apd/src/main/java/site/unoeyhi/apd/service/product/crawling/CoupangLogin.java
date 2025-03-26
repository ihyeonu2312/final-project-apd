// package site.unoeyhi.apd.service.product.crawling;

// import com.microsoft.playwright.*;
// import com.microsoft.playwright.options.LoadState;

// import org.springframework.stereotype.Service;

// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.util.List;
// import java.util.Map;
// import java.util.Scanner;

// @Service
// public class CoupangLogin {
//     private static final String COOKIES_PATH = "cookies.json";

//     public void loginAndSaveCookies() {
//         try (Playwright playwright = Playwright.create()) {
//             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
//                 .setHeadless(false)  // ✅ 반드시 headful 모드로 실행 (탐지 방지)
//                 .setChannel("chrome")
//                 .setArgs(List.of("--disable-http2", "--disable-blink-features=AutomationControlled")) // ✅ 자동화 탐지 방지 추가
//             );

//             BrowserContext context;

//             // ✅ 기존 로그인 세션 유지
//             if (Files.exists(Paths.get(COOKIES_PATH))) {
//                 context = browser.newContext(new Browser.NewContextOptions()
//                     .setStorageStatePath(Paths.get(COOKIES_PATH))
//                 );
//                 System.out.println("🔄 [쿠키 로드] 기존 세션 사용");
//             } else {
//                 context = browser.newContext();
//                 System.out.println("🆕 [새 세션] 새로운 브라우저 컨텍스트 생성");
//             }

//             Page page = context.newPage();

//             // ✅ Playwright 자동화 탐지 우회 (User-Agent, navigator 설정)
//             context.setExtraHTTPHeaders(Map.of(
//                 "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
//                 "Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"
//             ));

//             page.evaluate("() => { Object.defineProperty(navigator, 'webdriver', { get: () => undefined }); }");
//             page.evaluate("() => { window.chrome = {}; }");
//             page.evaluate("() => { navigator.permissions.query = (parameters) => Promise.resolve({ state: 'granted' }); }");

//             System.out.println("🔑 [로그인 진행] 쿠팡 로그인 페이지 이동...");
//             page.navigate("https://login.coupang.com/login/login.pang");

//             // ✅ 자동화 탐지 우회 코드 적용
//             page.evaluate("() => { Object.defineProperty(navigator, 'webdriver', { get: () => undefined }); }");
//             page.evaluate("() => { window.chrome = {}; }");
//             page.evaluate("() => { navigator.permissions.query = (parameters) => Promise.resolve({ state: 'granted' }); }");
//             page.evaluate("() => { console.debug = () => {}; }");

//             // ✅ 로그인 후 10초 대기
//             page.waitForTimeout(10000);
//             System.out.println("🔎 [로그인 후 페이지 URL] " + page.url());

//             // ✅ 자동 리디렉션 감지
//             try {
//                 page.waitForURL("**coupang.com/**", new Page.WaitForURLOptions().setTimeout(20000));
//                 System.out.println("🔄 [리디렉션 감지 완료] 현재 URL: " + page.url());
//             } catch (Exception e) {
//                 System.out.println("🚨 [자동 리디렉션 감지 실패] " + e.getMessage());
//             }

//             // ✅ 자동 리디렉션이 감지되지 않으면 강제 이동
//             if (page.url().contains("login.coupang.com")) {
//                 System.out.println("🚨 [자동 리디렉션 감지되지 않음] 강제 이동 시도...");
//                 page.evaluate("() => { window.location.href = 'https://www.coupang.com/'; }");
//                 page.waitForLoadState(LoadState.NETWORKIDLE);
//                 System.out.println("✅ [강제 리디렉션 완료] 현재 URL: " + page.url());
//             }

//             // ✅ 3초, 5초 후에도 URL 출력하여 상태 확인
//             System.out.println("🔎 [로그인 직후 URL] " + page.url());
//             page.waitForTimeout(3000);
//             System.out.println("🔎 [3초 후 URL] " + page.url());
//             page.waitForTimeout(5000);
//             System.out.println("🔎 [5초 후 URL] " + page.url());

//             // ✅ 로그인 상태 확인 (마이쿠팡 버튼 또는 로그아웃 버튼 감지)
//             boolean isLoggedIn = false;
//             Locator myCoupang = page.locator("a[data-coupang='mycoupang']");
//             Locator logoutButton = page.locator("a[href*='logout']");

//             if (myCoupang.isVisible()) {
//                 System.out.println("✅ [로그인 확인] 마이쿠팡 버튼 감지됨!");
//                 isLoggedIn = true;
//             } else if (logoutButton.isVisible()) {
//                 System.out.println("✅ [로그인 확인] 로그아웃 버튼 감지됨!");
//                 isLoggedIn = true;
//             } else {
//                 System.out.println("🚨 [로그인 확인 실패] 로그인 상태 확인 불가.");
//             }

//             // ✅ 로그인 후 쿠키 저장
//             if (isLoggedIn) {
//                 context.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("cookies.json")));
//                 System.out.println("✅ [로그인 세션 저장 완료] 쿠키 파일: " + Paths.get("cookies.json").toAbsolutePath());
//             } else {
//                 System.out.println("🚨 [로그인 세션 만료] 쿠키 저장 불가능");
//             }


//             // ✅ 로그인 후 페이지 새로고침
//             page.reload();
//             page.waitForLoadState(LoadState.NETWORKIDLE);
//             System.out.println("✅ [로그인 완료 후 페이지 새로고침 완료]");

//             browser.close();
//         } catch (Exception e) {
//             System.out.println("🚨 [오류 발생] " + e.getMessage());
//         }
//     }
// }
