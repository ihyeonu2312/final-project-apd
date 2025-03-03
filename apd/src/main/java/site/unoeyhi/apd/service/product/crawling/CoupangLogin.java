package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class CoupangLogin {
    private static final String COOKIES_PATH = "cookies.json";

    public void loginAndSaveCookies() {
      // ✅ 이미 쿠키가 존재하면 로그인 생략
      if (Files.exists(Paths.get(COOKIES_PATH))) {
        System.out.println("✅ [로그인 생략] 기존 쿠키 사용");
        return;
      } 
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            System.out.println("🔑 [로그인 진행] 쿠팡 로그인 페이지 이동...");
            page.navigate("https://login.coupang.com/login/login.pang");

            // ✅ 아이디 & 비밀번호 입력
            page.fill("#login-email-input", "tkddnjs3351@naver.com"); 
            page.fill("#login-password-input", "as940926!");

            // ✅ 로그인 버튼 클릭
            page.click("#login-button");

            // ✅ 로그인 성공 여부 확인 (사용자 메뉴가 보이면 성공)
            page.waitForSelector("#user-menu", new Page.WaitForSelectorOptions().setTimeout(10000));

            // ✅ 로그인 후 쿠키 저장
            context.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get(COOKIES_PATH)));

            System.out.println("✅ [로그인 완료] 쿠키 저장됨: " + COOKIES_PATH);

            browser.close();
        } catch (Exception e) {
            System.out.println("🚨 [로그인 실패] " + e.getMessage());
        }
    }
}
