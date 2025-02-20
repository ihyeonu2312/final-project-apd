package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.dto.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Option;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductOption;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.service.product.OptionService;
import site.unoeyhi.apd.service.product.ProductOptionService;
import site.unoeyhi.apd.service.product.ProductService;

import java.util.*;

@Service
@Log4j2
public class CrawlingService {

    @Autowired
    private ProductService productService;
    @Autowired
    private OptionService optionService;
    @Autowired
    private ProductOptionService productOptionService;
    @Autowired
    private CategoryRepository categoryRepository;

    private static final String BASE_URL = "https://www.coupang.com";

    private static final List<Map<String, Object>> CATEGORY_LIST = Arrays.asList(
            Map.of("targetCno", "176522", "categoryId", 1L)
            // Map.of("targetCno", "176522", "categoryId", 2L),
            // Map.of("targetCno", "416328", "categoryId", 3L),
            // Map.of("targetCno", "416289", "categoryId", 4L),
            // Map.of("targetCno", "434929", "categoryId", 5L)
    );

    public void crawling() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(true) // ✅ Headless 모드 활성화 (창을 띄우지 않음)
                .setArgs(Arrays.asList(
                        "--disable-blink-features=AutomationControlled", // ✅ 자동화 감지 우회
                        "--disable-gpu", // ✅ GPU 사용 비활성화
                        "--no-sandbox", // ✅ 샌드박스 모드 비활성화 (Linux 환경에서 필요)
                        "--disable-dev-shm-usage", // ✅ 공유 메모리 문제 방지
                        "--window-size=1920,1080" // ✅ 창 크기 설정
                ))
             );
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(getRandomUserAgent()) // ✅ 사용자 에이전트 추가
                .setExtraHTTPHeaders(Map.of(
                    "Referer", "https://www.coupang.com/",
                    "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
                    "Accept-Language", "ko-KR,ko;q=0.9",
                    "Upgrade-Insecure-Requests", "1"
                ))
                
            );
     
            // Page page = context.newPage();

        //     for (Map<String, Object> category : CATEGORY_LIST) {
        //         String targetCno = (String) category.get("targetCno");
        //         Long categoryId = (Long) category.get("categoryId");

        //         String categoryUrl = BASE_URL + "/np/categories/" + targetCno + "?listSize=10&sorter=bestAsc&page=5";
        //         page.navigate(categoryUrl);
        //         page.waitForLoadState(LoadState.NETWORKIDLE);
        //         page.navigate(categoryUrl, new Page.NavigateOptions().setTimeout(60000)); // 타임아웃 30초 설정
        //         page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        //         List<String> prodUrlList = new ArrayList<>();
        //         List<ElementHandle> prodElements = page.querySelectorAll("ul.browse-product-list a");

        //         for (ElementHandle element : prodElements) {
        //             String href = element.getAttribute("href");
        //             if (href != null && !href.isEmpty()) {
        //                 prodUrlList.add(BASE_URL + href);
        //             }
        //         }

        //         log.info("상품 목록 개수: " + prodUrlList.size());

        //         for (String productUrl : prodUrlList) {
        //             Map<String, Object> productDetails = scrapeProductDetails(page, productUrl);
        //             log.info(productDetails);
                
        //             if (!productDetails.isEmpty()) {
        //                 saveProductToDB(productDetails, categoryId); // ✅ categoryId를 함께 전달
        //             }
        //         }
                
        //     }
        }
    }
    private String getRandomUserAgent() {
        List<String> userAgents = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/109.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/120.0.0.0 Safari/537.36"
        );
        
        Random random = new Random();
        return userAgents.get(random.nextInt(userAgents.size()));
    }
    

    /**
     * ✅ 상품 상세 정보 크롤링 (누락된 부분 추가)
     */
    private void saveProductToDB(Map<String, Object> productDetails, Long categoryId) {
        if (categoryId == null) {
            log.error("카테고리 ID가 null입니다. 상품을 저장할 수 없습니다.");
            return;
        }
    
        String title = (String) productDetails.get("title");
        double price = (double) productDetails.get("price"); // 🚨 price 변환 필수
        List<String> prodImgs = (List<String>) productDetails.get("prod_img");
        List<String> prodDetails = (List<String>) productDetails.get("prod_detail");
        List<Map<String, Object>> optionsList = (List<Map<String, Object>>) productDetails.get("options");
    
        // ✅ 카테고리 존재 여부 확인 후 저장
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            log.warn("존재하지 않는 카테고리 ID: " + categoryId);
            return;
        }
        Category category = categoryOpt.get();
    
        // ✅ 기존 상품 여부 확인
        Optional<Product> existingProduct = productService.findByTitle(title);
        if (existingProduct.isPresent()) {
            log.info("이미 존재하는 상품: " + title);
            return;
        }
    
        // ✅ 상품 저장 (category 설정 추가)
        ProductDto productDto = ProductDto.builder()
                .name(title)
                .price(price)
                .categoryId(categoryId) // ✅ 카테고리 추가
                .build();
        Product product = productService.saveProduct(productDto);
    
        // ✅ 옵션 저장
        for (Map<String, Object> optionData : optionsList) {
            String optionType = (String) optionData.get("옵션명");
            List<String> optionValues = (List<String>) optionData.get("옵션 리스트");
    
            for (String optionValue : optionValues) {
                Option existingOption = optionService.findByTypeAndValue(optionType, optionValue).orElse(null);
    
                if (existingOption == null) {
                    existingOption = Option.builder()
                            .optionValueType(optionType)
                            .optionValue(optionValue)
                            .build();
                    existingOption = optionService.saveOption(existingOption);
                }
    
                ProductOption productOption = ProductOption.builder()
                        .product(product)
                        .option(existingOption)
                        .build();
                productOptionService.save(productOption);
            }
        }
    
        log.info("상품 저장 완료: " + product.getName());
    }
    private Map<String, Object> scrapeProductDetails(Page page, String productUrl) {
        Map<String, Object> productDetails = new HashMap<>();
        page.navigate(productUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    
        try {
            // 상품명
            String title = page.locator("h1.prod-buy-header__title").innerText();
            productDetails.put("title", title);
    
            // 상품 가격
            String priceText = page.locator("div.prod-price span.total-price").innerText();
            double price = Double.parseDouble(priceText.replaceAll("[^0-9]", ""));
            productDetails.put("price", price);
    
            // 썸네일 이미지
            List<String> prodImgUrls = page.locator("div.prod-image__item img").allInnerTexts();
            productDetails.put("prod_img", prodImgUrls);
    
            // 상품 상세 이미지
            List<String> prodDetailImgs = page.locator("div.vendor-item img").allInnerTexts();
            productDetails.put("prod_detail", prodDetailImgs);
    
            // 상품 옵션
            List<Map<String, Object>> options = new ArrayList<>();
            List<ElementHandle> optionElements = page.querySelectorAll("div.prod-option__item");
    
            for (ElementHandle optionElement : optionElements) {
                String optionTitle = optionElement.querySelector("span.title").innerText();
                List<String> optionValues = new ArrayList<>();
                List<ElementHandle> optionItems = optionElement.querySelectorAll("ul.prod-option__list li div.prod-option__dropdown-item-title strong");
    
                for (ElementHandle optionItem : optionItems) {
                    String optionValue = optionItem.innerText();
                    optionValues.add(optionValue);
                }
    
                Map<String, Object> optionData = new HashMap<>();
                optionData.put("옵션명", optionTitle);
                optionData.put("옵션 리스트", optionValues);
                options.add(optionData);
            }
    
            productDetails.put("options", options);
        } catch (Exception e) {
            log.error("상품 크롤링 중 오류 발생: " + e.getMessage());
        }
    
        return productDetails;
    }
    
}
