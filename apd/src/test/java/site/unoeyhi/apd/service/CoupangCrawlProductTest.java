package site.unoeyhi.apd.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import site.unoeyhi.apd.service.product.crawling.CoupangCrawlerService;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class CoupangCrawlProductTest {

    @Autowired
    private CoupangCrawlerService coupangCrawlerService;

    @Test
    @Rollback(false)
    public void testStartCrawling() {
        System.out.println("🚀 [테스트 실행] 크롤링 시작");

        // ✅ 크롤링 실행
        CompletableFuture<Void> future = coupangCrawlerService.startCrawling();

        // ✅ 크롤링 완료까지 대기
        future.join();

        System.out.println("✅ [테스트 완료] 크롤링 성공");
    }
}
