package site.unoeyhi.apd;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import site.unoeyhi.apd.service.AliExpressService;

@Component
public class AliExpressRunner implements CommandLineRunner {
    private final AliExpressService crawlerService;

    public AliExpressRunner(AliExpressService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 AliExpress 크롤링 실행 시작...");

        // ✅ 크롤링할 URL 설정 (원하는 카테고리 페이지로 변경 가능)
        String url = "https://www.aliexpress.com/category/100003109/women-clothing.html";

        // ✅ 크롤링 실행 (5개 제품 가져오기)
        crawlerService.crawlAndSaveProducts(url, 5);

        System.out.println("✅ 크롤링 완료!");
    }
}
