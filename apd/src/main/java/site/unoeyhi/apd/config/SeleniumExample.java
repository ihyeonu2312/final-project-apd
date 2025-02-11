package site.unoeyhi.apd.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class SeleniumExample {
  public static void main(String[] args) {
        // ChromeDriver 경로 설정
        System.setProperty("webdriver.chrome.driver", "C:/drivers/chromedriver.exe");

        // WebDriver 생성 및 페이지 이동
        WebDriver driver = new ChromeDriver();

        //페이지 이동
        driver.get("https://www.aliexpress.com");

        // 페이지 제목 출력
        System.out.println("Page title is: " + driver.getTitle());

        // 브라우저 종료
        driver.quit();
    }
  
}
