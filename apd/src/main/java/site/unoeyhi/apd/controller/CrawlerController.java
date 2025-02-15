package site.unoeyhi.apd.controller;

import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.service.AliExpressService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class CrawlerController {
    private final AliExpressService crawlerService;

    public CrawlerController(AliExpressService crawlerService) {
        this.crawlerService = crawlerService;
    }

    // // ✅ GET 요청으로 크롤링 실행
    // @GetMapping
    // public String crawlAliExpress(@RequestParam String url, @RequestParam(defaultValue = "5") int maxProducts) {
    //     crawlerService.crawlAndSaveProducts(url, maxProducts);
    //     return "✅ 크롤링이 완료되었습니다!";
    // }
        // ✅ 가전제품 크롤링 API
        @GetMapping("/home-appliances")
        public String crawlHomeAppliances() {
            crawlerService.crawlHomeAppliances();
            return "✅ 가전제품 크롤링 완료!";
        }
}
