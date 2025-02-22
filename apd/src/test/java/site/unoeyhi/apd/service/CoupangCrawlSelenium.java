package site.unoeyhi.apd.service;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoupangCrawlSelenium {
    private static final String DB_URL = "jdbc:mariadb://np.unoeyhi.site:3306/mifo";
    private static final String DB_USER = "mifo";
    private static final String DB_PASSWORD = "1234";
    private int count = 0;
    private static final String BASE_DATA_PATH = "C:\\coupang\\data\\";

    @Test
    public void testCrawlInit() {
        System.setProperty("webdriver.chrome.driver", "C:/chromedriver/chromedriver.exe");

        List<String> categories = getCategoryUrls(); // ✅ DB에서 카테고리 URL 가져옴
        for (String categoryUrl : categories) {
            String categoryId = extractCategoryId(categoryUrl); // ✅ 카테고리 ID 추출

            WebDriver driver = new ChromeDriver();
            driver.get(categoryUrl);
            sleep(3000);

            // ✅ 상품 목록에서 URL만 따로 저장
            List<String> productUrls = new ArrayList<>();
            try {
                List<WebElement> productItems = driver.findElements(By.cssSelector("#productList li a"));

                for (WebElement item : productItems) {
                    String itemHref = item.getAttribute("href");
                    if (itemHref != null && !itemHref.isEmpty()) {
                        productUrls.add(itemHref);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠ 상품 목록 가져오기 실패");
                e.printStackTrace();
            }
            driver.quit(); // ✅ 목록 크롤링 후 브라우저 닫기

            // ✅ 상품 상세 페이지 크롤링 (HTML 저장)
            for (String itemHref : productUrls) {
                driver = new ChromeDriver();
                driver.get(itemHref);
                sleep(3000);

                try {
                    handleAlert(driver);

                    // ✅ 상품 ID 추출 (잘못된 숫자 제거)
                    String productId = extractProductId(itemHref);

                    // ✅ 저장 폴더 생성 (C:\coupang\data\{categoryId}\{productId})
                    String productDataPath = BASE_DATA_PATH + categoryId + "\\" + productId + "\\";
                    Files.createDirectories(Paths.get(productDataPath));

                    // ✅ HTML 저장 (이미지 크롤링 없음)
                    savePageHtml(driver, productDataPath + "page.html");

                    count++;
                } catch (Exception e) {
                    System.out.println("⚠ 상세 페이지 크롤링 실패: " + itemHref);
                    e.printStackTrace();
                } finally {
                    driver.quit(); // ✅ 한 상품 크롤링 후 브라우저 닫기
                }
            }
        }
        System.out.println("✅ 총 상품 개수: " + count);
    }

    // **📌 Alert 자동 닫기**
    private void handleAlert(WebDriver driver) {
        try {
            Alert alert = driver.switchTo().alert();
            System.out.println("⚠ Alert 감지됨, 닫습니다.");
            alert.dismiss();
        } catch (NoAlertPresentException ignored) {
        }
    }

    // **📌 HTML 저장 함수**
    private void savePageHtml(WebDriver driver, String savePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(savePath))) {
            writer.write(driver.getPageSource());
            System.out.println("📄 HTML 저장 완료: " + savePath);
        } catch (IOException e) {
            System.out.println("⚠ HTML 저장 실패: " + savePath);
            e.printStackTrace();
        }
    }

    // **📌 URL에서 상품 ID 정확히 추출**
    private String extractProductId(String url) {
        String pattern = ".*/vp/products/(\\d+).*";  // `/vp/products/` 다음 숫자 찾기
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(url);

        if (matcher.matches()) {
            return matcher.group(1);  // 상품 ID만 추출
        }
        return "unknown";  // 상품 ID가 없는 경우 처리
    }

    // **📌 URL에서 카테고리 ID 추출**
    private String extractCategoryId(String url) {
        String[] parts = url.split("/");
        for (String part : parts) {
            if (part.matches("\\d{3,}")) { // 3자리 이상의 숫자를 카테고리 ID로 간주
                return part;
            }
        }
        return "unknown";
    }

    // **📌 DB에서 카테고리 URL 조회**
    private List<String> getCategoryUrls() {
        List<String> urlList = new ArrayList<>();
        String query = "SELECT url FROM tmp_category";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                urlList.add(rs.getString("url"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlList;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
