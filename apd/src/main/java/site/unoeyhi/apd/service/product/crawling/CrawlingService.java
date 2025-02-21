package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.unoeyhi.apd.dto.CrawledData;
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

    // private static final List<String> CATEGORY_TARGETS = Arrays.asList("176422", "564553", "178155", "115573");
    
    // ✅ 원하는 대분류 카테고리를 지정 (이름 그대로 사용)
    private static final Set<String> TARGET_CATEGORIES = Set.of(
    "패션의류/잡화","뷰티","가전디지털","홈인테리어","스포츠/레저"
    );

     public void saveCrawledProduct(CrawledData crawledData) {
        System.out.println("🚀 [크롤링된 데이터] " + crawledData);

        // CrawledData → ProductDto 변환
        ProductDto productDto = new ProductDto();
        productDto.setName(crawledData.getName());
        productDto.setDescription(crawledData.getDescription());
        productDto.setPrice(crawledData.getPrice());
        productDto.setStockQuantity(crawledData.getStockQuantity());
        productDto.setImageUrl(crawledData.getImageUrl());
        productDto.setCategoryId(crawledData.getCategoryId()); // ✅ categoryId 설정

        System.out.println("🚀 [크롤링 변환] 생성된 ProductDto: " + productDto);

        productService.saveProduct(productDto);
    }



    private Map<String, String> getMainCategories(Page page) {
        // ✅ 쿠팡 메인 페이지 이동
        page.navigate(BASE_URL, new Page.NavigateOptions().setTimeout(60000));
        page.waitForLoadState(LoadState.NETWORKIDLE);
    
        // ✅ 대분류 카테고리 목록 가져오기
        List<ElementHandle> categoryElements = page.querySelectorAll("ul.menu.shopping-menu-list > li > a.first-depth");
    
        Map<String, String> categoryMap = new HashMap<>();
        for (ElementHandle categoryElement : categoryElements) {
            String href = categoryElement.getAttribute("href");
            String categoryName = categoryElement.textContent().trim(); // ✅ 대분류 이름 가져오기
    
            if (href != null && href.contains("/np/categories/") && !categoryName.isEmpty()) {
                // ✅ 카테고리 ID 추출
                String categoryId = href.replaceAll("\\D+", ""); // 숫자만 추출
                categoryMap.put(categoryName, categoryId);
            }
        }
    
        log.info("✅ 크롤링된 대분류 카테고리 목록: " + categoryMap);
        return categoryMap;
    }
    
    
    // private List<String> getCategoryIds(Page page) {
    //     page.navigate(BASE_URL, new Page.NavigateOptions().setTimeout(60000)); // ✅ 쿠팡 메인 페이지 이동
    //     page.waitForLoadState(LoadState.NETWORKIDLE);
    
    //     // ✅ 카테고리 ID가 포함된 링크 찾기
    //     List<ElementHandle> categoryLinks = page.querySelectorAll("a[href*='/np/categories/']");
        
    //     List<String> categoryIds = new ArrayList<>();
    //     for (ElementHandle link : categoryLinks) {
    //         String href = link.getAttribute("href");
    //         if (href != null && href.contains("/np/categories/")) {
    //             String categoryId = href.replaceAll("\\D+", ""); // 숫자만 추출
    //             categoryIds.add(categoryId);
    //         }
    //     }
    
    //     log.info("✅ 크롤링된 카테고리 ID: " + categoryIds);
    //     return categoryIds;
    // }



    public void crawling() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(Arrays.asList(
                    "--disable-blink-features=AutomationControlled",
                    "--disable-gpu",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--window-size=1920,1080",
                    "--disable-http2" // ✅ HTTP2 강제 비활성화
                ))
            );
    
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .setIgnoreHTTPSErrors(true)
                .setExtraHTTPHeaders(Map.of(
                    "Referer", "https://www.coupang.com/",
                    "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
                    "Accept-Language", "ko-KR,ko;q=0.9",
                    "Upgrade-Insecure-Requests", "1",
                    "Connection", "keep-alive" // HTTP/1.1 연결 유지
                ))
            );
    
            Page page = context.newPage();
    
            // ✅ 대분류 카테고리 크롤링
            Map<String, String> categoryMap = getMainCategories(page);
    
            if (categoryMap.isEmpty()) {
                log.warn("❌ 대분류 카테고리를 찾을 수 없습니다. 크롤링을 중단합니다.");
                return;
            }
    
            for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                String categoryName = entry.getKey();
                String categoryId = entry.getValue();
    
                if (!TARGET_CATEGORIES.contains(categoryName)) {
                    log.info("🚫 제외된 카테고리: " + categoryName);
                    continue;
                }
    
                try {
                    Optional<Category> categoryOpt = categoryRepository.findByCategoryName(categoryName);

                    if (categoryOpt.isEmpty()) {
                        log.warn("❌ 존재하지 않는 카테고리: " + categoryName + " → 새로 추가");

                        Category newCategory = Category.builder()
                                .categoryName(categoryName)
                                .build();

                        Category savedCategory = categoryRepository.save(newCategory);
                        log.info("✅ 새 대분류 카테고리 추가 완료: " + savedCategory.getCategoryId());

                        categoryOpt = Optional.of(savedCategory);
                    }


    
                    Category category = categoryOpt.get();
                    log.info("✅ [crawling] categoryId 확인: " + category.getCategoryId());
    
                    // ✅ 카테고리별 URL
                    String categoryUrl = BASE_URL + "/np/categories/" + categoryId + "?listSize=10&sorter=bestAsc&page=1";
                    try {
                        page.navigate(categoryUrl, new Page.NavigateOptions()
                            .setTimeout(60000) // ✅ 타임아웃 60초로 증가
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED) // ✅ 네트워크 로드 대기 방식 변경
                        );
                        page.waitForLoadState(LoadState.NETWORKIDLE);
                    } catch (Exception e) {
                        log.error("❌ 페이지 이동 실패: " + categoryUrl, e);
                        continue;
                    }
    
                    // ✅ 상품 목록 크롤링 (공통 선택자 적용)
                    List<String> prodUrlList = new ArrayList<>();
                    List<ElementHandle> prodElements = page.querySelectorAll(
                        "a.baby-product-link, " +
                        "ul.browse-product-list a, " +
                        "a.product-link, " +
                        "div.product-card a, " +
                        "div.search-product a" // ✅ 추가적인 상품 선택자
                    );
    
                    if (prodElements.isEmpty()) {
                        log.warn("❌ 상품을 찾을 수 없습니다. 선택자 변경 필요");
                        continue;
                    }
    
                    // ✅ 상품 URL 가져오기 & 중복 제거
                    for (ElementHandle element : prodElements) {
                        String href = element.getAttribute("href");
                        if (href != null && !href.isEmpty()) {
                            String fullUrl = href.startsWith("http") ? href : BASE_URL + href;
                            if (!prodUrlList.contains(fullUrl)) {
                                prodUrlList.add(fullUrl);
                            }
                        }
                    }
    
                    log.info("✅ [" + categoryName + "] 크롤링한 상품 개수: " + prodUrlList.size());
    
                    // ✅ 상품 크롤링 실행
                    for (String productUrl : prodUrlList) {
                        try {
                            Map<String, Object> productDetails = scrapeProductDetails(page, productUrl);
    
                            if (productDetails.isEmpty()) {
                                log.warn("🚫 상품 정보가 비어 있음. 건너뜀: " + productUrl);
                                continue;
                            }
    
                            saveProductToDB(productDetails, category.getCategoryId());
                        } catch (Exception e) {
                            log.error("❌ 상품 크롤링 실패: " + productUrl, e);
                        }
                    }
    
                } catch (Exception e) {
                    log.error("❌ 크롤링 중 오류 발생: " + e.getMessage(), e);
                }
            }
        }
    }
    

            
    
    //         // ✅ 카테고리 ID 자동 크롤링
    //         List<String> categoryTargets = getCategoryIds(page);
    
    //         if (categoryTargets.isEmpty()) {
    //             log.warn("❌ 카테고리를 찾을 수 없습니다. 크롤링을 중단합니다.");
    //             return;
    //         }
    
    //         for (String targetCno : categoryTargets) {
    //             try {
    //                 Long categoryId = Long.parseLong(targetCno);
            
    //                 // ✅ DB에서 categoryId 기준으로 조회
    //                 Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
            
    //                 if (categoryOpt.isEmpty()) {
    //                     log.warn("❌ 존재하지 않는 카테고리 ID: " + categoryId + " → 새로 추가");
            
    //                     // ✅ 새로운 카테고리 생성 (categoryId 없이 저장 → AUTO_INCREMENT 사용)
    //                     Category newCategory = Category.builder()
    //                             .categoryName("쿠팡 카테고리 " + categoryId) // ✅ 임시 이름 설정
    //                             .build();
            
    //                     // ✅ save() 후 반환된 객체 사용 (ID 자동 할당)
    //                     Category savedCategory = categoryRepository.save(newCategory);
    //                     log.info("✅ 새 카테고리 추가 완료: " + savedCategory.getCategoryId());
            
    //                     categoryOpt = Optional.of(savedCategory);
    //                 }
            
    //                 // ✅ 카테고리 가져오기
    //                 Category category = categoryOpt.get();
            
    //                 // ✅ 카테고리 페이지 크롤링 시작
    //                 String categoryUrl = BASE_URL + "/np/categories/" + targetCno + "?listSize=10&sorter=bestAsc&page=1";
    //                 page.navigate(categoryUrl);
    //                 page.waitForLoadState(LoadState.NETWORKIDLE);
            
    //                 List<String> prodUrlList = new ArrayList<>();
    //                 List<ElementHandle> prodElements = page.querySelectorAll("ul.browse-product-list a");
            
    //                 for (ElementHandle element : prodElements) {
    //                     String href = element.getAttribute("href");
    //                     if (href != null && !href.isEmpty()) {
    //                         prodUrlList.add(BASE_URL + href);
    //                     }
    //                 }
            
    //                 log.info("✅ 상품 개수: " + prodUrlList.size());
            
    //                 // ✅ 각 상품 페이지 크롤링 시작
    //                 for (String productUrl : prodUrlList) {
    //                     saveProductToDB(scrapeProductDetails(page, productUrl), category.getCategoryId());
    //                 }
    //             } catch (Exception e) {
    //                 log.error("❌ 크롤링 중 오류 발생: " + e.getMessage(), e);
    //             }
    //         }
            
    //     }
    // }
    

    private String getRandomUserAgent() {
        List<String> userAgents = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:120.0) Gecko/20100101 Firefox/120.0"
        );

        return userAgents.get(new Random().nextInt(userAgents.size()));
    }
    

    /**
     * ✅ 상품 상세 정보 크롤링 (누락된 부분 추가)
     */
    private void saveProductToDB(Map<String, Object> productDetails, Long categoryId) {
        System.out.println("🚀 [saveProductToDB] 실행됨!");
        System.out.println("🚀 [saveProductToDB] categoryId: " + categoryId);
        System.out.println("🚀 [saveProductToDB] productDetails: " + productDetails);
    
        if (categoryId == null) {
            log.error("❌ 상품 저장 실패: categoryId가 NULL입니다.");
            return;
        }
    
        String title = (String) productDetails.get("title");
        double price = (double) productDetails.get("price");
        int stockQuantity = (int) productDetails.get("stock_quantity");
        List<String> prodImgs = (List<String>) productDetails.get("prod_img");
    
        // ✅ 기존 상품 확인
        if (productService.findByTitle(title).isPresent()) {
            log.info("이미 존재하는 상품: " + title);
            return;
        }
    
        String imageUrl = (prodImgs != null && !prodImgs.isEmpty()) ? prodImgs.get(0) : null;
        String description = "기본 상품 설명"; 
    
        // ✅ 상품 저장
        ProductDto productDto = ProductDto.builder()
                .name(title)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .categoryId(categoryId)
                .imageUrl(imageUrl)
                .build();
    
        Product savedProduct = productService.saveProduct(productDto);
        System.out.println("✅ [saveProductToDB] 저장된 상품: " + savedProduct.getName() + " (ID: " + savedProduct.getProductId() + ")");
    
        log.info("✅ 상품 저장 완료: " + savedProduct.getName() + " (ID: " + savedProduct.getProductId() + ")");
    
        // ✅ optionsList를 productDetails에서 가져오기 (❗ 오류 해결)
        List<Map<String, Object>> optionsList = (List<Map<String, Object>>) productDetails.get("options");
    
        // ✅ 옵션 저장
        if (optionsList != null && !optionsList.isEmpty()) {
            for (Map<String, Object> optionData : optionsList) {
                String optionType = (String) optionData.get("옵션명");
    
                // ✅ 옵션 리스트가 `null`이면 빈 리스트로 처리
                List<String> optionValues = (List<String>) optionData.get("옵션 리스트");
                if (optionValues == null) {
                    log.warn("⚠️ 옵션 리스트가 존재하지 않음: " + optionType);
                    optionValues = new ArrayList<>(); // ✅ 빈 리스트로 초기화
                }
    
                for (String optionValue : optionValues) {
                    Option option = optionService.findByTypeAndValue(optionType, optionValue)
                            .orElseGet(() -> {
                                Option newOption = Option.builder()
                                        .optionValueType(optionType)
                                        .optionValue(optionValue)
                                        .build();
                                return optionService.saveOption(newOption);
                            });
    
                    ProductOption productOption = ProductOption.builder()
                            .product(savedProduct)
                            .option(option)
                            .build();
                    productOptionService.save(productOption);
                }
            }
            log.info("✅ 옵션 저장 완료: " + savedProduct.getName());
        } else {
            log.info("⚠️ 옵션이 없는 상품: " + savedProduct.getName());
        }
    }
    

    
    
    
    
    private Map<String, Object> scrapeProductDetails(Page page, String productUrl) {
        Map<String, Object> productDetails = new HashMap<>();
        System.out.println("🚀 [scrapeProductDetails] 실행됨! URL: " + productUrl);
        page.navigate(productUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    
        try {
            // ✅ 상품명 가져오기
            try {
                String title = page.locator("h1.prod-buy-header__title").textContent();
                if (title == null || title.isEmpty()) {
                    log.warn("❗ 상품 제목이 존재하지 않음: " + productUrl);
                    return productDetails;
                }
                productDetails.put("title", title);
            } catch (Exception e) {
                log.error("❌ 상품 제목 크롤링 중 오류 발생: " + e.getMessage());
            }
    
            // ✅ 가격 가져오기
            try {
                String priceText = page.locator("div.prod-price span.total-price").textContent();
                double price = Double.parseDouble(priceText.replaceAll("[^0-9]", ""));
                productDetails.put("price", price);
            } catch (Exception e) {
                log.error("❌ 가격 크롤링 중 오류 발생: " + e.getMessage());
            }
    
            // ✅ 재고는 기본값 10으로 설정
            productDetails.put("stock_quantity", 10);
    
            // ✅ 이미지 가져오기
            List<String> prodImgUrls = page.querySelectorAll("div.prod-image__item img")
                .stream()
                .map(img -> img.getAttribute("src"))
                .filter(Objects::nonNull)
                .toList();
            productDetails.put("prod_img", prodImgUrls);
    
            // ✅ 상세 이미지 가져오기
            List<String> prodDetailImgs = page.querySelectorAll("div.vendor-item img")
                .stream()
                .map(img -> img.getAttribute("src"))
                .toList();
            productDetails.put("prod_detail", prodDetailImgs);
    
            // ✅ 옵션 가져오기
            List<Map<String, Object>> options = new ArrayList<>();
            List<ElementHandle> optionElements = page.querySelectorAll("div.prod-option__item");
    
            for (ElementHandle optionElement : optionElements) {
                String optionTitle = optionElement.querySelector("span.title").textContent();
                List<String> optionValues = new ArrayList<>();
                List<ElementHandle> optionItems = optionElement.querySelectorAll("ul.prod-option__list li div.prod-option__dropdown-item-title strong");
    
                for (ElementHandle optionItem : optionItems) {
                    optionValues.add(optionItem.textContent());
                }
    
                Map<String, Object> optionData = new HashMap<>();
                optionData.put("옵션명", optionTitle);
                optionData.put("옵션 리스트", optionValues);
                options.add(optionData);
            }
    
            // ✅ options 데이터를 추가해야 옵션이 정상적으로 저장됨!
            productDetails.put("options", options);
    
        } catch (Exception e) {
            log.error("❌ 상품 크롤링 중 오류 발생: " + e.getMessage());
        }
    
        return productDetails;
    }
    
    
    
    
}
