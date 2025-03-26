// package site.unoeyhi.apd.service.product.crawling;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.EnableAsync;
// import org.springframework.stereotype.Service;
// import com.microsoft.playwright.*;

// import site.unoeyhi.apd.dto.product.ProductDto;
// import site.unoeyhi.apd.entity.Category;
// import site.unoeyhi.apd.repository.CategoryRepository;
// import site.unoeyhi.apd.service.product.ProductService;

// import java.util.*;
// import java.util.concurrent.CompletableFuture;

// @Service
// @EnableAsync
// public class CoupangCrawlerService {

//     private final CategoryRepository categoryRepository;
//     private final ProductCrawler productCrawler;
//     private final ProductService productService;

//     private final Playwright playwright;
//     private final Browser browser;

//     @Autowired
//     public CoupangCrawlerService(CategoryRepository categoryRepository, 
//                                  ProductCrawler productCrawler, 
//                                  ProductService productService) {
//         this.categoryRepository = categoryRepository;
//         this.productCrawler = productCrawler;
//         this.productService = productService;

//         // ✅ Playwright & Browser 인스턴스를 한 번만 생성 (메모리 누수 방지)
//         this.playwright = Playwright.create();
//         this.browser = playwright.chromium().launch(
//             new BrowserType.LaunchOptions()
//                     .setHeadless(false)
//                     .setChannel("chrome")
//                     .setArgs(List.of(
//                             "--disable-blink-features=AutomationControlled",
//                             "--disable-features=BlockThirdPartyCookies",
//                             "--disable-web-security"
//                     ))
//         );
//     }

//     private static final int MAX_CATEGORY_CRAWL = 15; // ✅ 최대 15개 카테고리 크롤링
//     private static final int MAX_PRODUCTS_PER_CATEGORY = 10; // ✅ 각 카테고리당 10개 상품만 크롤링

//     // ✅ URL 기반으로 카테고리 ID 매핑
//     private static final Map<String, Long> CATEGORY_MAP = Map.ofEntries(
//         Map.entry("/fashion/", 1L),
//         Map.entry("/electronics/", 2L),
//         Map.entry("/beauty/", 3L),
//         Map.entry("/home/", 4L),
//         Map.entry("/sports/", 5L),
//         Map.entry("/automotive/", 6L),
//         Map.entry("/baby/", 7L),
//         Map.entry("/books/", 8L),
//         Map.entry("/food/", 9L),
//         Map.entry("/health/", 10L),
//         Map.entry("/toys/", 11L),
//         Map.entry("/office/", 12L),
//         Map.entry("/pet/", 13L),
//         Map.entry("/music/", 14L),
//         Map.entry("/movies/", 15L)
//     );


//     /**
//      * ✅ Playwright 브라우저 컨텍스트 생성 메서드
//      */
//     private BrowserContext createBrowserContext() {
//         return browser.newContext(new Browser.NewContextOptions()
//                 .setIsMobile(false)
//                 .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.6998.36 Safari/537.36")
//                 .setExtraHTTPHeaders(Map.of(
//                         "Accept-Language", "ko,ko-KR;q=0.9,en-US;q=0.8,en;q=0.7",
//                         "Accept-Encoding", "gzip, deflate, br, zstd",
//                         "Sec-Ch-Ua", "Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24",
//                         "Sec-Ch-Ua-Mobile", "?0",
//                         "Sec-Ch-Ua-Platform", "\"Windows\"",
//                         "Sec-Fetch-Dest", "document",
//                         "Sec-Fetch-User", "?1",
//                         "Upgrade-Insecure-Requests", "1",
//                         "Referer", "https://www.coupang.com/"
//                 ))
//         );
//     }

//     // /**
//     //  * ✅ 카테고리 URL을 기반으로 `categoryId` 찾기
//     //  */
//     // private Long getCategoryIdFromUrl(String categoryUrl) {
//     //     Optional<Category> categoryOpt = categoryRepository.findByUrl(categoryUrl); // ✅ DB에서 검색
//     //     return categoryOpt.map(Category::getCategoryId).orElse(0L); // ✅ 없으면 기본값 반환
//     // }


//     /**
//      * ✅ 카테고리 크롤링 후 상품 크롤링 & 저장 실행
//      */
//     public CompletableFuture<Void> startCrawling() {
//         System.out.println("🚀 [크롤링 시작] 카테고리 크롤링 진행");

//             return crawlAllCategories()
//             .thenComposeAsync(optionalCategoryMap -> CompletableFuture.runAsync(() -> {
//                 Map<String, Long> categoryMap = optionalCategoryMap.orElse(Map.of());
//                 BrowserContext context = createBrowserContext();

//                 for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
//                     String categoryUrl = entry.getKey();
//                     Long categoryId = entry.getValue();
//                     System.out.println("🔗 [크롤링할 카테고리] " + categoryUrl + " | ID: " + categoryId);

//                     crawlAndSaveProducts(context, categoryUrl, categoryId);
//                 }

//                 context.close();
//             }));
//     }

//     /**
//      * ✅ 카테고리 목록 가져오기
//      */
//     public CompletableFuture<Optional<Map<String, Long>>> crawlAllCategories() {
//         return CompletableFuture.supplyAsync(() -> {
//             System.out.println("🚀 [카테고리 크롤링 시작]");
    
//             List<Category> categories = categoryRepository.findAll();
//             if (categories.isEmpty()) {
//                 System.out.println("🚨 [크롤링 중단] 크롤링할 카테고리가 없습니다!");
//                 return Optional.empty();
//             }
    
//             // ✅ 최대 MAX_CATEGORY_CRAWL 개수만 선택
//             int limit = Math.min(categories.size(), MAX_CATEGORY_CRAWL);
//             List<Category> selectedCategories = categories.subList(0, limit);
    
//             // ✅ 카테고리 URL과 categoryId를 Map으로 저장
//             Map<String, Long> categoryMap = new HashMap<>();
//             for (Category category : selectedCategories) {
//                 String fullUrl = "https://www.coupang.com" + category.getUrl();
//                 categoryMap.put(fullUrl, category.getCategoryId());
//                 System.out.println("🔗 [카테고리] " + fullUrl + " | ID: " + category.getCategoryId());
//             }
    
//             return Optional.of(categoryMap);
//         });
//     }
    

//     /**
//      * ✅ 상품 크롤링 후 저장
//      */
//     private void crawlAndSaveProducts(BrowserContext context, String categoryUrl, Long categoryId) {
//         System.out.println("🚀 [CoupangCrawler] 카테고리 상품 크롤링 시작: " + categoryUrl + " | 카테고리 ID: " + categoryId);
    
//         List<ProductDto> products = productCrawler.crawlAllProducts(context, categoryUrl, MAX_PRODUCTS_PER_CATEGORY, categoryId);
        
//         if (products.isEmpty()) {
//             System.out.println("🚨 [크롤링된 상품 없음] 저장 중단.");
//             return;
//         }
    
//         for (ProductDto productDto : products) {
//             System.out.println("📦 [상품 저장 시도]: " + productDto.getName());
    
//             // ✅ categoryId 설정 후 저장
//             ProductDto savedProductDto = productDto.toBuilder()
//                     .categoryId(categoryId)
//                     .build();
    
//             productService.saveProduct(savedProductDto);
//         }
    
//         System.out.println("✅ [상품 저장 완료] 카테고리 ID: " + categoryId);
//     }
    

// }    
