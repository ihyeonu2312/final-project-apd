package site.unoeyhi.apd.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;
import site.unoeyhi.apd.service.product.crawling.CoupangCrawlerService;

@SpringBootTest
public class CoupangCrawlProductTest {

    @Autowired
    private CoupangCrawlerService coupangCrawlerService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testCrawlAllCategories() {
        System.out.println("🚀 [테스트] 모든 카테고리에서 상품 크롤링 시작!");
        coupangCrawlerService.crawlAllCategories();
        System.out.println("✅ [테스트 완료] 상품 크롤링 완료!");
    }
}
