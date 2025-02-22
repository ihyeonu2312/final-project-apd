package site.unoeyhi.apd.service;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoupangCrawlSelenium {
    private static final String DB_URL = "jdbc:mariadb://np.unoeyhi.site:3306/mifo";
    private static final String DB_USER = "mifo";
    private static final String DB_PASSWORD = "1234";

    @Test
    public void testCrawlInit() {
        System.setProperty("webdriver.chrome.driver", "C:/chromedriver/chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        driver.get("https://www.coupang.com/"); // ✅ 쿠팡 메인 페이지 접근
        sleep(3000);

        List<String> categoryUrls = new ArrayList<>();
        try {
            // ✅ 카테고리 목록 가져오기
            List<WebElement> categoryElements = driver.findElements(By.cssSelector(".nav-list > li > a"));

            for (WebElement categoryElement : categoryElements) {
                String categoryName = categoryElement.getText().trim();
                String categoryUrl = categoryElement.getAttribute("href");

                if (categoryUrl != null && categoryUrl.contains("/np/categories/")) {
                    Long coupangCategoryId = extractCategoryId(categoryUrl);

                    // ✅ 크롤링한 데이터 DB에 저장
                    saveCategoryToDB(categoryName, coupangCategoryId, categoryUrl);

                    // ✅ URL 리스트에 추가 (추후 상품 크롤링을 위해)
                    categoryUrls.add(categoryUrl);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ 카테고리 크롤링 실패");
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        System.out.println("✅ 카테고리 크롤링 완료. 상품 크롤링 시작...");
        crawlProducts(categoryUrls);
    }

    // **📌 상품 크롤링 메서드**
    private void crawlProducts(List<String> categories) {
        for (String categoryUrl : categories) {
            String categoryId = String.valueOf(extractCategoryId(categoryUrl)); // ✅ 카테고리 ID 추출

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

            // ✅ 상품 상세 페이지 크롤링
            for (String itemHref : productUrls) {
                driver = new ChromeDriver();
                driver.get(itemHref);
                sleep(3000);

                try {
                    handleAlert(driver);

                    // ✅ 상품 ID 추출
                    String productId = extractProductId(itemHref);

                    System.out.println("✅ 크롤링 완료 - 상품 ID: " + productId);
                } catch (Exception e) {
                    System.out.println("⚠ 상세 페이지 크롤링 실패: " + itemHref);
                    e.printStackTrace();
                } finally {
                    driver.quit(); // ✅ 한 상품 크롤링 후 브라우저 닫기
                }
            }
        }
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
    private Long extractCategoryId(String url) {
        String pattern = ".*/np/categories/(\\d+).*";  // `/np/categories/` 다음 숫자 찾기
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(url);

        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));  // 숫자로 변환
        }
        return null;
    }

    // **📌 DB에 카테고리 저장**
    private void saveCategoryToDB(String categoryName, Long coupangCategoryId, String url) {
        String query = "INSERT INTO category (category_name, coupang_category_id, url) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), url = VALUES(url)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, categoryName);
            pstmt.setLong(2, coupangCategoryId);
            pstmt.setString(3, url);
            pstmt.executeUpdate();

            System.out.println("✅ 카테고리 저장 완료: " + categoryName + " (" + coupangCategoryId + ")");
        } catch (Exception e) {
            System.out.println("⚠ 카테고리 저장 실패: " + categoryName);
            e.printStackTrace();
        }
    }

    // **📌 DB에서 카테고리 URL 조회**
    private List<String> getCategoryUrls() {
        List<String> urlList = new ArrayList<>();
        String query = "SELECT url FROM category";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String url = rs.getString("url");
                System.out.println("🔹 카테고리 URL 가져옴: " + url);
                urlList.add(url);
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
