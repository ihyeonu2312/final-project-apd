package site.unoeyhi.apd.service;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

@Service
@Log4j2
public class CrawlingService {
    private static final String URL = "https://www.coupang.com/vp/products/7853440364?itemId=21412385914&vendorItemId=88468707216&sourceType=CATEGORY&categoryId=520353&isAddedCart=";

    public Map<String, Object> fetchProductDetails() {
        ChromeOptions options = new ChromeOptions();

        // Headless 모드 설정 가능 (일단은 UI 보이도록 설정)
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        // User-Agent 변경 (차단 방지)
        options.addArguments("--user-agent=" + getRandomUserAgent());

        WebDriver driver = null;
        Map<String, Object> productDetails = new HashMap<>();

        try {
            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            driver.get(URL);

            // JavaScript 실행 완료 대기
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));

            // 1️⃣ **상품명 가져오기 (XPath)**
            WebElement titleElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(@class, 'prod-buy-header__title')]")
            ));
            productDetails.put("title", titleElement.getText());

            // 2️⃣ **대표 이미지 가져오기 (XPath)**
            WebElement imageElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//img[contains(@class, 'prod-image__detail')]")
            ));
            productDetails.put("image_url", imageElement.getAttribute("src"));

            WebElement priceElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id=\"contents\"]/div[2]/div[1]/div[3]/div[5]/div[1]/div/div[2]/span")
            ));
            productDetails.put("price", priceElement.getText());

            // 4️⃣ **옵션 정보 가져오기 (XPath)**
            List<WebElement> optionElements = driver.findElements(By.xpath("//button[contains(@class, 'prod-option__selected')]/span[@class='value']"));
            List<String> optionss = new ArrayList<>();
            for (WebElement option : optionElements) {
              optionss.add(option.getText());
            }
            productDetails.put("options", optionss);

            return productDetails;

        } catch (NoSuchElementException e) {
            log.error("요소를 찾을 수 없습니다: " + e.getMessage());
            productDetails.put("error", "페이지 구조 변경으로 인해 요소를 찾을 수 없습니다.");
        } catch (TimeoutException e) {
            log.error("페이지 로딩이 너무 오래 걸렸거나 구조가 변경됨: " + e.getMessage());
            productDetails.put("error", "페이지 로딩 시간 초과 또는 구조 변경됨");
        } catch (Exception e) {
            log.error("크롤링 오류 발생: " + e.getMessage());
            productDetails.put("error", "크롤링 오류 발생: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return productDetails;
    }

    private String getRandomUserAgent() {
        List<String> userAgents = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/537.36",
            "Mozilla/5.0 (iPad; CPU OS 16_5 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/537.36"
        );
        Collections.shuffle(userAgents);
        return userAgents.get(0);
    }
}
