// package site.unoeyhi.apd.common;

// import java.util.List;

// import org.hibernate.sql.ast.tree.expression.AliasedExpression;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import site.unoeyhi.apd.service.ProductService;

// @Component
// public class Crawler implements CommandLineRunner {

//     @Autowired
//     private AliasedExpression aliExpress;

//     @Autowired
//     private ProductService productService;

//     @Override
//     public void run(String... args) {
//         // 알리익스프레스에서 상품 데이터 크롤링
//         String url = "https://www.aliexpress.com/category/100003109/women-clothing.html";
//         List<String> productNames = aliExpress.crawlAliExpressProducts(url);

//         // 크롤링한 데이터를 DB에 저장
//         productService.saveCrawledProducts(productNames);

//         System.out.println("크롤링 및 저장 완료!");
//     }
// }
