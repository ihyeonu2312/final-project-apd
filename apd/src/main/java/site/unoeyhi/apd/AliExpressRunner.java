// package site.unoeyhi.apd;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
// import site.unoeyhi.apd.service.AliExpressService;

// @Component
// public class AliExpressRunner implements CommandLineRunner {
//     private final AliExpressService crawlerService;

//     public AliExpressRunner(AliExpressService crawlerService) {
//         this.crawlerService = crawlerService;
//     }

//     @Override
//     public void run(String... args) throws Exception {
//         System.out.println("🚀 AliExpress 가전제품 크롤링 실행 시작...");

//         // ✅ 가전제품 크롤링 실행 (이전 의류 크롤링 코드 제거)
//         crawlerService.crawlHomeAppliances();

//         System.out.println("✅ 가전제품 크롤링 완료!");
//     }
// }