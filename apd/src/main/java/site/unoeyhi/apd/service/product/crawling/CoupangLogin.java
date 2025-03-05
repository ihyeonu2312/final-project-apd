package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class CoupangLogin {
    private static final String COOKIES_PATH = "cookies.json";

    public void loginAndSaveCookies() {
        try {
            // ✅ 기존 쿠키 파일 삭제
            Files.deleteIfExists(Paths.get(COOKIES_PATH));
            System.out.println("🛑 [쿠키 삭제] 기존 쿠키 파일 제거 완료.");
        } catch (Exception e) {
            System.out.println("🚨 [쿠키 삭제 실패] " + e.getMessage());
        }
    
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
    
            System.out.println("🔑 [로그인 진행] 쿠팡 로그인 페이지 이동...");
            page.navigate("https://login.coupang.com/login/login.pang");
    
            // ✅ Playwright 자동화 탐지 우회
            page.evaluate("() => { Object.defineProperty(navigator, 'webdriver', { get: () => false }); }");
    
            // ✅ 로그인 폼이 로드될 때까지 기다림
            page.waitForSelector("#login-email-input", new Page.WaitForSelectorOptions().setTimeout(10000));
    
            // ✅ 아이디 & 비밀번호 입력
            page.fill("#login-email-input", "your_email@naver.com");
            page.fill("#login-password-input", "your_password");
    
            // ✅ 로그인 버튼 클릭
            Locator loginButton = page.locator("#login-button");
            page.waitForSelector("#login-button", new Page.WaitForSelectorOptions().setTimeout(10000));
            if (loginButton.count() > 0) {
                loginButton.click();
                System.out.println("✅ [로그인 시도] 로그인 버튼 클릭");
            } else {
                System.out.println("🚨 [오류] 로그인 버튼을 찾을 수 없음");
                return;
            }
    
            // ✅ 로그인 성공 여부 확인
            try {
                page.waitForSelector("#user-menu", new Page.WaitForSelectorOptions().setTimeout(15000));
                System.out.println("✅ [로그인 성공]");
            } catch (Exception e) {
                System.out.println("🚨 [로그인 실패] 사용자 메뉴가 나타나지 않음: " + e.getMessage());
                return;
            }
    
            // ✅ 로그인 후 쿠키 저장
            context.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get(COOKIES_PATH)));
            System.out.println("✅ [로그인 완료] 쿠키 저장됨: " + COOKIES_PATH);
    
            browser.close();
        } catch (Exception e) {
            System.out.println("🚨 [로그인 실패] " + e.getMessage());
        }
    }
}
