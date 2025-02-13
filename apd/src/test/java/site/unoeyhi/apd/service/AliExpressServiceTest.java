package site.unoeyhi.apd.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AliExpressServiceTest {
  
    @Autowired
    private AliExpressService aliExpressService;

    @Test
    public void testCrawling() {
        Long adminId = 1L;  // 관리자 ID (테스트용)
        List<String> products = aliExpressService.fetchProductDetails("https://ko.aliexpress.com/category/100003109/women-clothing.html", 10, adminId);
        System.out.println("크롤링된 상품 목록: " + products);
    }
}

