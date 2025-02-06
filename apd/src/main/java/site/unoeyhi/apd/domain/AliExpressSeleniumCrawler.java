package site.unoeyhi.apd.domain;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AliExpressSeleniumCrawler {

    public static void main(String[] args) {
        // 크롬 드라이버 경로 설정
        System.setProperty("webdriver.chrome.driver", "C:/path/to/chromedriver.exe");

        // 크롬 브라우저 실행
        WebDriver driver = new ChromeDriver();
        driver.get("https://ko.aliexpress.com/wholesale?SearchText=laptop");

        // MariaDB 연결 정보
        String jdbcUrl = "jdbc:mariadb://unoeyhi:3306/mifo"; // MariaDB URL
        String dbUser = "your_db_user"; // 데이터베이스 사용자
        String dbPassword = "your_db_password"; // 데이터베이스 비밀번호

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            // 상품 목록 가져오기
            List<WebElement> products = driver.findElements(By.cssSelector("div.product-snippet_Title__content"));
            
            for (WebElement product : products) {
                String title = product.getText();
                String link = product.findElement(By.tagName("a")).getAttribute("href");

                // 가격 크롤링 (CSS 선택자 예시: span.price)
                String price = "";
                try {
                    price = product.findElement(By.cssSelector("span.price")).getText();
                } catch (Exception e) {
                    System.out.println("가격 정보를 찾을 수 없습니다.");
                }

                // 상품 이미지 URL 크롤링
                String imageUrl = "";
                try {
                    WebElement imageElement = product.findElement(By.cssSelector("img"));
                    imageUrl = imageElement.getAttribute("src");
                } catch (Exception e) {
                    System.out.println("이미지 URL을 찾을 수 없습니다.");
                }

                // 상품 데이터 DB에 삽입
                insertProductToDatabase(conn, title, link, price, imageUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 브라우저 종료
            driver.quit();
        }
    }

    private static void insertProductToDatabase(Connection conn, String title, String link, String price, String imageUrl) {
        String insertQuery = "INSERT INTO Products (name, description, price, stock_quantity, category_id, created_at, image_url) "
                           + "VALUES (?, ?, ?, ?, ?, NOW(), ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, title);  // 상품 이름
            stmt.setString(2, "상품 설명");  // 상품 설명 (크롤링 코드에 설명을 추가할 수 있다면 이 부분을 수정)
            stmt.setString(3, price);  // 가격
            stmt.setInt(4, 100);  // 재고 수량 (임시값, 크롤링할 수 있다면 해당 값으로 수정)
            stmt.setInt(5, 1);  // 카테고리 ID (임시값, 필요 시 적절한 카테고리 ID로 수정)
            stmt.setString(6, imageUrl);  // 이미지 URL
            
            stmt.executeUpdate();
            System.out.println("상품 정보가 데이터베이스에 저장되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}