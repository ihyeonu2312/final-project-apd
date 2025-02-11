// package site.unoeyhi.apd.crawler;

// import java.util.List;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import site.unoeyhi.apd.service.AliExpress;
// import site.unoeyhi.apd.service.ProductService;

// @SpringBootTest
// public class CrawlerTest {

//     @Autowired
//     private AliExpress aliExpress;

//     @Autowired
//     private ProductService productService;

//     @Test
//     public void testCrawlAndSave() {
//         String url = "https://www.aliexpress.com/category/100003109/women-clothing.html";
//         List<String> productNames = aliExpress.crawlAliExpressProducts(url);
//         productService.saveCrawledProducts(productNames);

//         System.out.println("크롤링 및 저장 완료!");
//     }
// }