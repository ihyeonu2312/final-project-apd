package site.unoeyhi.apd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;
import site.unoeyhi.apd.service.product.crawling.CoupangCrawlerService;

import java.util.List;

@SpringBootTest
public class CoupangCrawlProductTest {

    @Autowired
    private CoupangCrawlerService coupangCrawlerService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;


    @Test
    @Rollback(false)
    void testCrawlAllCategories() {
        System.out.println("🚀 [테스트] 모든 카테고리에서 상품 크롤링 시작!");

        // ✅ 크롤링할 카테고리 확인
        List<Category> categories = categoryRepository.findAll();
        Assertions.assertFalse(categories.isEmpty(), "🚨 카테고리가 없습니다! 크롤링할 데이터가 없음.");

        // ✅ 크롤링 실행
        coupangCrawlerService.crawlAllCategories();

        // ✅ 크롤링 후 상품이 저장되었는지 검증
        long productCount = productRepository.count();
        Assertions.assertTrue(productCount > 0, "🚨 크롤링 후 저장된 상품이 없습니다!");

        System.out.println("✅ [테스트 완료] 상품 크롤링 완료! 저장된 상품 개수: " + productCount);
    }
}
