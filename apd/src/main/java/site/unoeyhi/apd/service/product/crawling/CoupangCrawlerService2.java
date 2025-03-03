// package site.unoeyhi.apd.service.product.crawling;

// import com.microsoft.playwright.*;
// import com.microsoft.playwright.options.Cookie;
// import com.microsoft.playwright.options.WaitUntilState;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.scheduling.annotation.EnableAsync;
// import org.springframework.stereotype.Service;
// import site.unoeyhi.apd.dto.product.OptionDto;
// import site.unoeyhi.apd.dto.product.ProductDto;
// import site.unoeyhi.apd.entity.Category;
// import site.unoeyhi.apd.entity.Product;
// import site.unoeyhi.apd.repository.CategoryRepository;
// import site.unoeyhi.apd.service.product.DiscountService;
// import site.unoeyhi.apd.service.product.ProductService;
// import site.unoeyhi.apd.service.product.crawling.*;

// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Random;
// import java.util.Scanner;
// import java.util.Set;
// import java.util.concurrent.CompletableFuture;

// @Service
// @EnableAsync
// public class CoupangCrawlerService {

//     private static final Path COOKIE_PATH = Paths.get("cookies.json");

//     private final CategoryRepository categoryRepository;
//     private final ProductService productService;
//     private final DiscountService discountService;

//     @Autowired
//     public CoupangCrawlerService(CategoryRepository categoryRepository,
//                                  ProductService productService, DiscountService discountService , ProductCrawler productCrawler) {
//         this.categoryRepository = categoryRepository;
//         this.productService = productService;
//         this.discountService = discountService;
//         this.productCrawler = productCrawler;
//     }

//     private void performLogin(BrowserContext context) {
//         Page loginPage = context.newPage();
//         loginPage.navigate("https://login.coupang.com/login/login.pang");
    
//         // ✅ 로그인 정보 입력 (아이디/비밀번호 직접 입력)
//         loginPage.fill("#login-email-input", "@"); // 🛑 아이디 입력
//         loginPage.fill("#login-password-input", "!"); // 🛑 비밀번호 입력
//         loginPage.click("#login-button"); // ✅ 로그인 버튼 클릭
    
//         // ✅ 로그인 완료될 때까지 대기
//         loginPage.waitForSelector("#user-menu", new Page.WaitForSelectorOptions().setTimeout(10000));
    
//         // ✅ 로그인 후 쿠키 저장
//         context.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("cookies.json")));
    
//         System.out.println("✅ [로그인 완료] 쿠키 저장됨.");
//     }

    
//     @Async
//     public CompletableFuture<Void> crawlAllCategories() {
//         System.out.println("🚀 [크롤링 시작] 모든 카테고리 크롤링");

//         List<Category> categories = categoryRepository.findAll();
//         if (categories.isEmpty()) {
//             System.out.println("🚨 [크롤링 중단] 크롤링할 카테고리가 없습니다!");
//             return CompletableFuture.completedFuture(null);
//         }

//         List<CompletableFuture<Void>> futures = new ArrayList<>();
//         for (Category category : categories) {
//             System.out.println("📌 [카테고리] ID: " + category.getCategoryId() + " | Name: " + category.getCategoryName());
//             CompletableFuture<Void> future = CompletableFuture.runAsync(() -> crawlProductsByCategory(category));
//             futures.add(future);
//         }

//         return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//     }

//     /**
//      * ✅ 개별 카테고리 크롤링
//      */
//     public void crawlProductsByCategory(Category category) {
//         String categoryUrl = "https://www.coupang.com" + category.getUrl();
        
//         try (Playwright playwright = Playwright.create()) {
//             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
//             BrowserContext context = createOrLoadContext(browser);
            
//             if (context == null) {
//                 System.out.println("🚨 [오류] `context` 초기화 실패, 크롤링을 중단합니다.");
//                 return;
//             }
            
//             // ✅ 크롤링 수행
//             try {
//                 crawlProducts(context, categoryUrl, category);
//             } finally {  // ✅ context를 안전하게 닫기
//                 if (context != null) {
//                     context.close();
//                 }
//             }
//         } catch (Exception e) {  
//             System.out.println("🚨 [크롤링 오류] " + e.getMessage());
//         }  
        
        
    
                
//     /**
//      * ✅ 개별 상품 크롤링 메서드 (crawlProductsByCategory에서 호출됨)
//      */
//     private void crawlProducts(BrowserContext context, String categoryUrl, Category category) {
//         if (context == null) {
//             System.out.println("🚨 [오류] `context`가 null이므로 크롤링 중단.");
//             return;
//         }

//         Page page = context.newPage();
//         page.navigate(categoryUrl, new Page.NavigateOptions().setTimeout(60000).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

//         if (page.url().equals("about:blank") || page.title().isEmpty()) {
//             System.out.println("🚨 [경고] 페이지 로드 실패! 크롤링 중단");
//             return;
//         }

//         List<ElementHandle> productElements = page.querySelectorAll("li.baby-product.renew-badge");
//         if (productElements == null || productElements.isEmpty()) {
//             System.out.println("🚨 [경고] 상품이 없음! 크롤링 중단.");
//             return;
//         }

//         System.out.println("📦 [총 상품 개수] " + productElements.size());

//         int count = 0;
//         for (ElementHandle productElement : productElements) {
//             if (productElement == null || count >= 30) continue;

//             ElementHandle nameElement = productElement.querySelector("div.name");
//             String name = (nameElement != null && nameElement.textContent() != null) ? nameElement.textContent().trim() : "알 수 없음";
//             System.out.println("🏷️ [상품] " + name);

//             ElementHandle linkElement = productElement.querySelector("a.baby-product-link");
            
//             if (linkElement == null) {
//                 System.out.println("🚨 [경고] 상품 링크 없음! 건너뜀");
//                 continue;
//             }

//             String detailUrl = linkElement.getAttribute("href");
//             if (detailUrl == null || detailUrl.trim().isEmpty()) {
//                 System.out.println("🚨 [경고] 상세 URL이 없음! 건너뜀");
//                 continue;
//             }

//             detailUrl = "https://www.coupang.com" + detailUrl;
//             System.out.println("✅ [DEBUG] detailUrl 생성됨: " + detailUrl);

//             Page detailPage = openDetailPage(context, detailUrl);
//             if (detailPage == null) {
//                 System.out.println("🚨 [오류] 상세 페이지를 열 수 없어 크롤링 건너뜀.");
//                 continue;
//             }

//             // ✅ 상품 정보 크롤링 수행
//             System.out.println("✅ [크롤링 성공] " + detailPage.title());

//             // ✅ 상품 상세 정보 크롤링 로직 추가
//             try {
//                 String productTitle = detailPage.locator("h2.prod-buy-header__title").textContent().trim();
//                 System.out.println("🛒 [상품명] " + productTitle);
//             } catch (Exception e) {
//                 System.out.println("🚨 [오류] 상품명 크롤링 실패: " + e.getMessage());
//             }

//             // ✅ 상세 페이지 크롤링 후 닫기
//             if (!detailPage.isClosed()) {
//                 detailPage.close();
//             }


//             count++;
//         }
//     }

//     /**
//      * ✅ 상품 상세 페이지 열기 메서드
//      */
//     private Page openDetailPage(BrowserContext context, String detailUrl) {
//         if (context == null) {
//             System.out.println("🚨 [오류] `context`가 null입니다. 페이지를 열 수 없습니다.");
//             return null;
//         }

//         Page detailPage = context.newPage();
        
//         try {
//             Response response = detailPage.navigate(detailUrl, new Page.NavigateOptions()
//                 .setTimeout(60000)
//                 .setWaitUntil(WaitUntilState.LOAD)
//             );

//             if (response == null || response.status() != 200) {
//                 System.out.println("🚨 [오류] 페이지 로드 실패: " + detailUrl);
//                 detailPage.close();
//                 return null;
//             }

//             if (detailPage.url().equals("about:blank") || detailPage.title().isEmpty()) {
//                 System.out.println("🚨 [경고] `about:blank` 감지됨. 5초 후 새로고침...");
//                 detailPage.waitForTimeout(5000);
//                 detailPage.reload();
//                 detailPage.waitForTimeout(5000);

//                 if (detailPage.title().isEmpty()) {
//                     System.out.println("🚨 [실패] 여전히 `about:blank`. 페이지 닫기.");
//                     if (!detailPage.isClosed()) {
//                         detailPage.close();
//                     }
//                     return null;
//                 }
//             }
    
//             return detailPage;
//         } catch (PlaywrightException e) {
//             System.out.println("🚨 [오류] 페이지 로드 중 예외 발생: " + e.getMessage());
//             if (!detailPage.isClosed()) {
//                 detailPage.close();
//             }
//             return null;
//         }
//     }
        
        

//         // ✅ 상품 정보 크롤링 수행
//         System.out.println("✅ [크롤링 성공] " + detailPage.title());

//         // ✅ 상품 상세 정보 크롤링 로직 추가
//         String productTitle = detailPage.locator("h2.prod-buy-header__title").textContent().trim();
//         System.out.println("🛒 [상품명] " + productTitle);

        
        
            
//             System.out.println("✅ [성공] 상세 페이지 크롤링 시작: " + detailPage.url());
            
//             // ✅ 가격 크롤링
//             Locator originalPriceLocator = detailPage.locator("span.origin-price").first();
//             Locator discountPriceLocator = detailPage.locator("span.discount-price").first();
            
//             String originalPriceText = originalPriceLocator.count() > 0 ? originalPriceLocator.textContent() : "";
//             String discountPriceText = discountPriceLocator.count() > 0 ? discountPriceLocator.textContent() : "";
            
//             double originalPrice = 0.0;
//             double discountPrice = 0.0;
            
//             try {
//                 if (originalPriceText != null && !originalPriceText.trim().isEmpty() && originalPriceText.matches(".*\\d.*")) {
//                     originalPrice = Double.parseDouble(originalPriceText.replaceAll("[^0-9]", ""));
//                 }
//                 if (discountPriceText != null && !discountPriceText.trim().isEmpty() && discountPriceText.matches(".*\\d.*")) {
//                     discountPrice = Double.parseDouble(discountPriceText.replaceAll("[^0-9]", ""));
//                 }
//             } catch (NumberFormatException e) {
//                 System.out.println("🚨 [오류] 가격 변환 실패: " + e.getMessage());
//             }
            
//             double finalPrice = (discountPrice > 0) ? discountPrice : originalPrice;
            
//             System.out.println("💰 [디버깅] 원가: " + originalPrice);
//             System.out.println("💰 [디버깅] 할인 가격: " + discountPrice);
//             System.out.println("💰 [디버깅] 최종 가격: " + finalPrice);
//         }
    
    
//                     // ✅ 대표 이미지 크롤링
//                     Locator imageLocator = detailPage.locator("div.prod-image img").first();
//                     String imageUrl = imageLocator.isVisible() ? imageLocator.getAttribute("src") : "";

//                     // ✅ 추가 이미지 크롤링
//                     List<String> additionalImages = new ArrayList<>();
//                     for (Locator imgLocator : detailPage.locator("div.prod-image img").all()) {
//                         if (imgLocator.isVisible()) {
//                             String imgSrc = imgLocator.getAttribute("src");
//                             if (imgSrc != null && !imgSrc.trim().isEmpty() && !imgSrc.equals(imageUrl)) {
//                                 additionalImages.add(imgSrc);
//                             }
//                         }
//                     }
                 
//                     // ✅ 기본 옵션 리스트 생성
//                     List<OptionDto> optionList = new ArrayList<>();
//                     Set<String> optionSet = new HashSet<>(); // 중복 방지

                    

//                     // ✅ `optionWrapper` 내부 옵션 체크
//                     Locator optionWrapper = detailPage.locator("#optionWrapper");
                    

//                     // ✅ 옵션이 있는지 먼저 확인
//                     if (optionWrapper.count() > 0) {
//                         Locator optionLocator = optionWrapper.locator("li");
//                         if (optionLocator.count() > 0) {
//                             for (Locator option : optionLocator.all()) {
//                                 String optionText = option.textContent().trim();
//                                 if (!optionText.isEmpty() && !optionSet.contains(optionText)) {
//                                     optionSet.add(optionText);
//                                     optionList.add(new OptionDto("OPTION", optionText));
//                                 }
//                             }
//                         }
//                     }

//                     // ✅ `optionWrapper`가 존재하는지 먼저 확인
//                     // ✅ 옵션 요소가 존재하는지 확인한 후 `waitForSelector` 실행
//                     if (detailPage.locator("div.prod-option, ul.Image_Select__items, div.tab-selector__tab").count() > 0) {
//                         System.out.println("✅ 옵션 요소 감지됨. 크롤링 대기...");
//                         detailPage.waitForSelector(
//                             "div.prod-option, ul.Image_Select__items, div.tab-selector__tab",
//                             new Page.WaitForSelectorOptions().setTimeout(5000) // ✅ 대기 시간 단축
//                         );
//                     } else {
//                         System.out.println("⚠️ [경고] 옵션 요소 없음. 기본 옵션 처리.");
//                     }

//                         // ✅ 옵션 요소가 로드될 때까지 대기
//                         try {
//                             detailPage.waitForSelector(
//                                 "div.prod-option, ul.Image_Select__items, div.tab-selector__tab",
//                                 new Page.WaitForSelectorOptions().setTimeout(15000)
//                             );
//                         } catch (TimeoutError e) {
//                             System.out.println("⚠️ [옵션 없음] 해당 상품은 옵션이 없습니다. 기본 옵션 처리.");
//                         }

//                         // ✅ 옵션 크롤링을 위한 `Locator` 리스트 생성
//                         List<Locator> optionLocators = Arrays.asList(
//                             detailPage.locator("ul.prod-option__item li"),
//                             detailPage.locator("div.Dropdown-Select.prod-option__item"),
//                             detailPage.locator("div.prod-option__selected-container button")
//                         );

//                         // ✅ 각 옵션을 탐색하여 중복 없이 추가
//                         for (Locator locator : optionLocators) {
//                             if (locator.count() > 0) {
//                                 for (Locator option : locator.all()) {
//                                     String optionValue = option.textContent();
//                                     if (optionValue == null || optionValue.trim().isEmpty()) {
//                                         optionValue = "단일 상품"; // 기본값 설정
//                                     }
//                                     if (!optionSet.contains(optionValue.trim())) {
//                                         optionSet.add(optionValue.trim());
//                                         optionList.add(new OptionDto("OPTION", optionValue.trim()));
//                                     }
//                                 }
//                             }
//                         }
//                         // ✅ 드롭다운 옵션 크롤링
//                         Locator dropdownOptions = detailPage.locator("ul.prod-option__item li");
//                         for (Locator option : dropdownOptions.all()) {
//                             String optionText = option.textContent().trim();
//                             if (!optionText.isEmpty() && !optionSet.contains(optionText)) {
//                                 optionSet.add(optionText);
//                                 optionList.add(new OptionDto("드롭다운 옵션", optionText));
//                             }
//                         }

//                         // ✅ 이미지 옵션 크롤링 (추가)
//                         Locator imageOptions = detailPage.locator("ul.Image_Select__items li");
//                         for (Locator option : imageOptions.all()) {
//                             String optionValue = option.getAttribute("data-thumbnail-image-url");
//                             if (optionValue == null) {
//                                 optionValue = option.getAttribute("data-origin-image-url");
//                             }
//                             if (optionValue != null && !optionSet.contains(optionValue)) {
//                                 optionSet.add(optionValue);
//                                 optionList.add(new OptionDto("이미지 옵션", optionValue));
//                             }
//                         }
//                         // ✅ 탭 옵션 크롤링 (추가)
//                         Locator tabContainer = detailPage.locator("div.tab-selector-container");
//                         if (tabContainer.count() > 0) {
//                             Locator tabOptions = tabContainer.locator("div.tab-selector__tab");
//                             for (Locator option : tabOptions.all()) {
//                                 String optionText = option.locator("div.tab-selector__tab-title").textContent().trim();
//                                 if (!optionSet.contains(optionText)) {
//                                     optionSet.add(optionText);
//                                     optionList.add(new OptionDto("탭 옵션", optionText));
//                                 }
//                             }
//                         }
//                         // ✅ 표 형식 옵션 크롤링 (추가)
//                         Locator optionContainer = detailPage.locator("div.prod-option");
//                         if (optionContainer.count() > 0) {
//                             List<Locator> optionRows = optionContainer.locator("tr").all();
//                             for (Locator row : optionRows) {
//                                 String optionTitle = row.locator("span.title").textContent().trim();
//                                 String optionValue = row.locator("span.value").textContent().trim();
//                                 if (!optionTitle.isEmpty() && !optionValue.isEmpty() && !optionSet.contains(optionValue)) {
//                                     optionSet.add(optionValue);
//                                     optionList.add(new OptionDto(optionTitle, optionValue));
//                                 }
//                             }
//                         }
//                         // ✅ 추가 상품 옵션 (번들 옵션) 크롤링 (추가)
//                         Locator bundleOptions = detailPage.locator("div.bundle-option");
//                         for (Locator option : bundleOptions.all()) {
//                             String bundleText = option.textContent().trim();
//                             if (!bundleText.isEmpty() && !optionSet.contains(bundleText)) {
//                                 optionSet.add(bundleText);
//                                 optionList.add(new OptionDto("번들 옵션", bundleText));
//                             }
//                         }

//                         // ✅ 라디오 버튼 옵션 크롤링 (추가)
//                         Locator radioOptions = detailPage.locator("input[type='radio']");
//                         for (Locator option : radioOptions.all()) {
//                             String optionText = option.getAttribute("value");
//                             if (optionText != null && !optionSet.contains(optionText)) {
//                                 optionSet.add(optionText);
//                                 optionList.add(new OptionDto("라디오 버튼 옵션", optionText));
//                             }
//                         }

//                         // ✅ 셀렉트 박스 옵션 크롤링 (추가)
//                         Locator selectOptions = detailPage.locator("select option");
//                         for (Locator option : selectOptions.all()) {
//                             String optionText = option.textContent().trim();
//                             if (!optionText.isEmpty() && !optionSet.contains(optionText)) {
//                                 optionSet.add(optionText);
//                                 optionList.add(new OptionDto("셀렉트 옵션", optionText));
//                             }
//                         }

//                         // ✅ 텍스트 입력형 옵션 크롤링 (개선)
//                         Locator textInputOptions = detailPage.locator("input[type='text']");
//                         for (Locator option : textInputOptions.all()) {
//                             String placeholder = option.getAttribute("placeholder");
//                             String value = option.getAttribute("value"); // ✅ 입력값도 확인
//                             if ((placeholder != null && !optionSet.contains(placeholder)) || 
//                                 (value != null && !optionSet.contains(value))) {
                                
//                                 optionSet.add(placeholder != null ? placeholder : value);
//                                 optionList.add(new OptionDto("텍스트 입력 옵션", placeholder != null ? placeholder : value));
//                             }
//                         }

//                         // ✅ 옵션별 가격 변동 크롤링 (옵션 값과 함께 저장)
//                         Locator priceChangeOptions = detailPage.locator("span.price-change");
//                         for (Locator option : priceChangeOptions.all()) {
//                             String priceText = option.textContent().trim();
//                             Locator parentOption = option.locator(".."); // 부모 요소에서 옵션 이름 찾기
//                             String optionName = parentOption.textContent().trim();
                            
//                             if (!priceText.isEmpty() && !optionSet.contains(priceText)) {
//                                 String finalText = optionName + " (" + priceText + ")";
//                                 optionSet.add(finalText);
//                                 optionList.add(new OptionDto("옵션별 가격 변동", finalText));
//                             }
//                         }

//                         // ✅ 옵션이 없는 경우 기본 옵션 추가
//                         if (optionList.isEmpty()) {
//                             optionList.add(new OptionDto("기본 옵션", "단일 상품"));
//                         }

//                         // ✅ 크롤링된 옵션 출력
//                         for (OptionDto option : optionList) {
//                             System.out.println("🛠 [옵션 크롤링] " + option);
//                         }


//                     // ✅ 상품 데이터 저장
//                     ProductDto productDto = ProductDto.builder()
//                             .name(name)
//                             .price(finalPrice)
//                             .stockQuantity(10)
//                             .categoryId(category.getCategoryId())
//                             .imageUrl(imageUrl)
//                             .thumbnailImageUrl(imageUrl)
//                             .detailUrl(detailUrl)
//                             .options(optionList)
//                             .additionalImages(additionalImages)
//                             .build();

//                             Product savedProduct = productService.saveProduct(productDto);
//                             if (savedProduct == null) {
//                                 System.out.println("🚨 [saveProduct] 상품 저장 실패로 크롤링 종료!");
//                                 return;
//                             } else {
//                                 System.out.println("✅ [상품 저장 성공] ID: " + savedProduct.getProductId() + " | 이름: " + savedProduct.getName());
//                             }
                            

//                     // ✅ 할인 정보 저장
//                     if (originalPrice > discountPrice) {
//                         discountService.saveDiscount(savedProduct, "PERCENT", (originalPrice - discountPrice) / originalPrice * 100);
//                     }
//                     saveCookies(context);
//                     count++;
//                     try {
//                         // 크롤링 로직 실행
//                     } catch (Exception e) {
//                         System.out.println("🚨 [오류 발생] " + e.getMessage());
//                     } finally {
//                         // ✅ 자원 정리 (예외 발생 여부와 관계없이 실행)
//                         if (context != null) {
//                             try {
//                                 context.close();
//                                 System.out.println("✅ [컨텍스트 종료]");
//                             } catch (Exception e) {
//                                 System.out.println("🚨 [컨텍스트 닫기 실패] " + e.getMessage());
//                             }
//                         }
                        
//                         if (browser != null) {
//                             try {
//                                 browser.close();
//                                 System.out.println("✅ [브라우저 종료]");
//                             } catch (Exception e) {
//                                 System.out.println("🚨 [브라우저 닫기 실패] " + e.getMessage());
//                             }
//                         }
//                     }
                
                    
    
//         /**
//          * ✅ 쿠키 기반 로그인 유지
//          */
//         public BrowserContext createOrLoadContext(Browser browser) {
//             BrowserContext context;
//             if (Files.exists(COOKIE_PATH)) {
//                 context = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(COOKIE_PATH));
//                 if (isUserLoggedIn(context)) {
//                     System.out.println("✅ [쿠키 로그인 성공] 기존 쿠키 사용.");
//                     return context;
//                 } else {
//                     System.out.println("🚨 [쿠키 만료] 다시 로그인 필요.");
//                     context = loginAndSaveCookies(browser);
//                 }
//             } else {
//                 System.out.println("🚨 [쿠키 없음] 로그인 필요.");
//                 context = loginAndSaveCookies(browser);
//             }
//             return context;
//         }
           
    
//     /**
//      * ✅ 로그인 후 쿠키 저장
//      */
//     private BrowserContext loginAndSaveCookies(Browser browser) {
//         BrowserContext context = browser.newContext();
//         Page page = context.newPage();
//         page.navigate("https://login.coupang.com/", new Page.NavigateOptions().setTimeout(60000));

//         System.out.println("🛑 [로그인 필요] 브라우저에서 로그인 후 엔터를 입력하세요...");
//         new Scanner(System.in).nextLine();

//         saveCookies(context);
//         return context;
//     }
    
//        /**
//      * ✅ 쿠키 저장
//      */
//     private void saveCookies(BrowserContext context) {
//         try {
//             context.storageState(new BrowserContext.StorageStateOptions().setPath(COOKIE_PATH));
//             System.out.println("✅ [쿠키 저장 완료] " + COOKIE_PATH.toAbsolutePath());
//         } catch (Exception e) {
//             System.out.println("🚨 [쿠키 저장 실패] " + e.getMessage());
//         }
//     }
// }
        
  