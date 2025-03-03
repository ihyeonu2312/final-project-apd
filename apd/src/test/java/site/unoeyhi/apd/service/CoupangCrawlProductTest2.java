// package site.unoeyhi.apd.service;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.Assertions;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.annotation.Rollback;

// import com.microsoft.playwright.*;

// import site.unoeyhi.apd.repository.CategoryRepository;
// import site.unoeyhi.apd.repository.product.ProductRepository;
// import site.unoeyhi.apd.service.product.crawling.CoupangCrawlerService;

// import java.nio.file.Files;
// import java.nio.file.Paths;

// import java.util.concurrent.CompletableFuture;


// @SpringBootTest
// public class CoupangCrawlProductTest {

//     @Autowired
//     private CoupangCrawlerService coupangCrawlerService;

//     @Autowired
//     private CategoryRepository categoryRepository;

//     @Autowired
//     private ProductRepository productRepository;


//     @Test
//     @Rollback(false)
//     void testCrawlAllCategoriesWithAsync() throws Exception {
//         System.out.println("🚀 [테스트] 로그인된 상태에서 상품 크롤링 시작!");

//         // ✅ `cookies.json`이 존재하는지 확인
//         java.nio.file.Path cookiePath = Paths.get("cookies.json");
//         Assertions.assertTrue(Files.exists(cookiePath), "🚨 쿠키 파일이 존재하지 않습니다! 먼저 로그인 후 쿠키 저장 필요.");

//         // ✅ 크롤링 실행
//         CompletableFuture<Void> future = coupangCrawlerService.crawlAllCategories();
        
//         // ✅ 크롤링이 끝날 때까지 대기
//         future.get();

//         // ✅ 크롤링 후 상품이 저장되었는지 검증
//         long productCount = productRepository.count();
//         Assertions.assertTrue(productCount > 0, "🚨 크롤링 후 저장된 상품이 없습니다!");

//         System.out.println("✅ [테스트 완료] 저장된 상품 개수: " + productCount);
//     }


//     // @Test
//     // @Rollback(false)
//     // public void testCrawling() {
//     //     coupangCrawlerService.crawlAllCategories();
//     //     // 크롤링 후 결과 확인 (예: DB에 저장된 데이터 개수 체크)
//     // }



// }
