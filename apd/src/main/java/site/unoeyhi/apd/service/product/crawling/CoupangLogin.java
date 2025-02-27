package site.unoeyhi.apd.service.product.crawling;

import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class CoupangLogin {
  public static void main(String[] args) {
      try (Playwright playwright = Playwright.create()) {
          Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
          BrowserContext context = browser.newContext();

          Page page = context.newPage();
          page.navigate("https://login.coupang.com/login/login.pang");

          // ✅ 로그인 정보 입력
          page.fill("input#login-email-input", "@naver."); // 이메일 입력
          page.fill("input#login-password-input", ""); // 비밀번호 입력
          page.locator("button._loginSubmitButton").hover();
          page.waitForTimeout(500);  // 버튼 활성화 대기
          page.locator("button._loginSubmitButton").click();


          // ✅ 10초 대기 (자동 로그인 실패 시 직접 로그인 가능)
          page.waitForTimeout(10000);

          // ✅ 쿠키 저장
          
          context.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("cookies.json")));

          System.out.println("✅ 쿠키 저장 완료!");
      }
  }
}