package site.unoeyhi.apd.service;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
public class AliExpress {
  public List<String> crawlAliExpressProducts(String url) {
        // ChromeDriver 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // 브라우저 UI 숨기기 (필요에 따라 제거 가능)
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);
        List<String> productNames = new ArrayList<>();

        try {
            // 페이지 로드
            driver.get(url);

            // 상품 이름 요소 선택 (HTML 구조에 맞게 변경 필요)
            List<WebElement> productElements = driver.findElements(By.cssSelector(".product-title-text"));

            // 각 상품 이름을 추출하여 리스트에 추가
            for (WebElement element : productElements) {
                productNames.add(element.getText());
            }
             // 크롤링된 데이터를 콘솔에 출력
             System.out.println("크롤링된 상품 이름 목록: " + productNames);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 브라우저 종료
            driver.quit();
        }

        return productNames;
    }
}

