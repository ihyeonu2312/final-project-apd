package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class PlaywrightTest {
    static Playwright playwright;
    static Browser browser;

    @BeforeAll
    static void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @AfterAll
    static void teardown() {
        browser.close();
        playwright.close();
    }

    @Test
    void testPageLoad() {
        Page page = browser.newPage();
        page.navigate("https://example.com");
        assertNotNull(page.title());
    }
}
