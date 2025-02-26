package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.dto.product.OptionDto;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.service.product.DiscountService;
import site.unoeyhi.apd.service.product.ProductService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class CoupangCrawlerService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final DiscountService discountService;

    public CoupangCrawlerService(CategoryRepository categoryRepository,
                                 ProductService productService, DiscountService discountService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
        this.discountService = discountService;
    }
    
    private String generateRandomIP() {
        Random random = new Random();
        return random.nextInt(256) + "." + 
            random.nextInt(256) + "." + 
            random.nextInt(256) + "." + 
            random.nextInt(256);
    }
    
    private Page openDetailPage(BrowserContext context, String detailUrl) {
        Page detailPage = context.newPage();
        int retryCount = 0;
        boolean success = false;
    
        while (!success && retryCount < 3) {
            try {
                System.out.println("🔄 [재시도 " + (retryCount + 1) + "] 상품 페이지 로딩 중: " + detailUrl);
    
                int randomDelay = new Random().nextInt(5000) + 2000; // 2~7초 랜덤 대기
                detailPage.waitForTimeout(randomDelay);
    
                // ✅ 페이지 이동
                Response response = detailPage.navigate(detailUrl, new Page.NavigateOptions()
                    .setTimeout(120000)
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                );
    
                // ✅ 응답 상태 체크 (200 OK 확인)
                if (response == null || response.status() != 200) {
                    System.out.println("🚨 [경고] 응답 상태 오류: " + (response != null ? response.status() : "NULL"));
                    retryCount++;
                    continue;
                }
    
                // ✅ `about:blank` 상태인지 확인 후 새로고침 시도
                if (detailPage.url().equals("about:blank") || detailPage.title().isEmpty()) {
                    System.out.println("🚨 [경고] `about:blank` 감지됨. 5초 대기 후 다시 확인...");
                    detailPage.waitForTimeout(5000);
                    if (detailPage.title().isEmpty()) {
                        System.out.println("🚨 [실패] `about:blank` 상태 지속. 페이지 로드 실패.");
                        retryCount++;
                        continue;
                    }
                }
    
                // ✅ 상품 제목 크롤링
                String productTitle = "상품명 없음";
                Locator mainTitleLocator = detailPage.locator("h1.prod-buy-header__title");
                if (mainTitleLocator.isVisible()) {
                    productTitle = mainTitleLocator.textContent().trim();
                    System.out.println("✅ [성공] 상품 제목 크롤링 완료: " + productTitle);
                }
    
                // ✅ 상품 가격 크롤링
                String priceText = detailPage.locator("span.total-price").textContent().trim();
                double productPrice = Double.parseDouble(priceText.replaceAll("[^0-9]", ""));
    
                // ✅ 상품 이미지 크롤링
                String imageUrl = detailPage.locator("div.prod-image img").first().getAttribute("src");
    
                System.out.println("✅ [최종 상품 정보] 제목: " + productTitle + " | 가격: " + productPrice + " | 이미지: " + imageUrl);
    
                success = true;
    
            } catch (PlaywrightException e) {
                System.out.println("🚨 [경고] 페이지 로드 실패: " + e.getMessage());
                retryCount++;
            }
        }
    
        // ✅ 최종적으로도 실패하면 null 반환
        if (!success) {
            System.out.println("🚨 [실패] 상세 페이지 로드 실패: " + detailUrl);
            if (!detailPage.isClosed()) {
                detailPage.close();
            }
            return null;
        }
    
        return detailPage;
    }
    
    
    
    public void crawlAllCategories() {
        System.out.println("🚀 [테스트] 모든 카테고리에서 상품 크롤링 시작!");
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            System.out.println("🚨 [크롤링 중단] 크롤링할 카테고리가 없습니다!");
            return;
        }

        for (Category category : categories) {
            System.out.println("📌 [카테고리] ID: " + category.getCategoryId() + " | Name: " + category.getCategoryName());
            crawlProductsByCategory(category);
        }
        System.out.println("✅ [크롤링 완료]");
    }

    public void crawlProductsByCategory(Category category) {
        String categoryUrl = "https://www.coupang.com" + category.getUrl();

            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(List.of(
                    "--disable-http2",  // ✅ HTTP/2 비활성화 (중요)
                    "--disable-blink-features=AutomationControlled",
                    "--disable-gpu",
                    "--disable-dev-shm-usage", // ✅ 메모리 부족 해결
                    "--disable-web-security" // ✅ 크로스 도메인 차단 해제
                )));

        
            Map<String, String> headers = new HashMap<>();
            headers.put("Upgrade-Insecure-Requests", "1");
            headers.put("Connection", "keep-alive");


            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
            .setIgnoreHTTPSErrors(true) // HTTPS 오류 무시
            .setJavaScriptEnabled(true) // JavaScript 활성화
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36") // ✅ 최신 버전 반영
            .setExtraHTTPHeaders(Map.of(
                "Accept-Language", "ko-KR,ko;q=0.9",
                "Referer", "https://www.coupang.com/",
                "X-Forwarded-For", generateRandomIP() // ✅ 무작위 IP 적용        
            ))
        );
        
        // ✅ Playwright 봇 감지 방지 코드 추가
        context.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => undefined });");
        context.addInitScript("window.navigator.chrome = { runtime: {} };");
        context.addInitScript("Object.defineProperty(navigator, 'languages', { get: () => ['ko-KR', 'ko'] });");
        context.addInitScript("Object.defineProperty(navigator, 'platform', { get: () => 'Win32' });");
        context.addInitScript("Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 4 });");
        context.addInitScript("Object.defineProperty(navigator, 'deviceMemory', { get: () => 8 });");
        context.addInitScript("Object.defineProperty(navigator, 'maxTouchPoints', { get: () => 1 });");
        context.addInitScript("Object.defineProperty(navigator, 'vendor', { get: () => 'Google Inc.' });");
        context.addInitScript("Object.defineProperty(navigator, 'userAgentData', { get: () => undefined });");

        // ✅ Coupang의 `canvas fingerprinting` 탐지를 우회
        context.addInitScript("HTMLCanvasElement.prototype.toDataURL = () => 'data:image/png;base64,FAKE_IMAGE';");
        context.addInitScript("WebGLRenderingContext.prototype.getParameter = () => 'FAKE_WEBGL';");
        context.addInitScript("RTCPeerConnection = function() { return {}; };");

            Page currentPage = context.newPage();
            currentPage.navigate(categoryUrl, new Page.NavigateOptions().setTimeout(120000).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            List<ElementHandle> productElements = currentPage.querySelectorAll("li.baby-product.renew-badge");
            if (productElements.isEmpty()) {
                System.out.println("🚨 상품 없음 (선택자 확인 필요)");
                return;
            }

            int count = 0;
            for (ElementHandle productElement : productElements) {
                if (count >= 30) break;

                ElementHandle nameElement = productElement.querySelector("div.name");
                String name = (nameElement != null) ? nameElement.innerText().trim() : "알 수 없음";
                System.out.println("🏷️ [디버깅] 상품명: " + name);

                ElementHandle linkElement = productElement.querySelector("a.baby-product-link");
                String detailUrl = (linkElement != null) ? "https://www.coupang.com" + linkElement.getAttribute("href") : "";
                System.out.println("🔍 [디버깅] 상품 상세 URL: " + detailUrl);

                Page detailPage = openDetailPage(context, detailUrl);
                if (detailPage == null) {
                    System.out.println("🚨 [오류] 상품 상세 페이지를 열 수 없어 크롤링 건너뜀.");
                    continue;
                }

                
                // ✅ 상품 제목 크롤링
                Locator productTitleLocator = detailPage.locator("h1.prod-buy-header__title");
                if (!productTitleLocator.isVisible()) {
                    System.out.println("🚨 [오류] 상품 제목 찾을 수 없음! 크롤링 건너뜀.");
                    detailPage.close();
                    continue;
                }
                System.out.println("✅ [성공] 상품 제목: " + productTitleLocator.textContent());
               
                    
                    System.out.println("✅ [성공] 상세 페이지 크롤링 시작: " + detailUrl);
                    // ✅ 가격 크롤링
                    Locator originalPriceLocator = detailPage.locator("span.origin-price").first();
                    Locator discountPriceLocator = detailPage.locator("span.discount-price").first();
                    String originalPriceText = originalPriceLocator.count() > 0 ? originalPriceLocator.textContent().trim() : "";
                    String discountPriceText = discountPriceLocator.count() > 0 ? discountPriceLocator.textContent().trim() : "";
                    
                    // ✅ 가격 파싱 예외 처리
                    double originalPrice = 0.0;
                    double discountPrice = 0.0;
                    try {
                        if (!originalPriceText.isEmpty() && originalPriceText.matches(".*\\d.*")) {  
                            originalPrice = Double.parseDouble(originalPriceText.replaceAll("[^0-9]", ""));
                        }
                        if (!discountPriceText.isEmpty() && discountPriceText.matches(".*\\d.*")) {  
                            discountPrice = Double.parseDouble(discountPriceText.replaceAll("[^0-9]", ""));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("🚨 [오류] 가격 변환 실패: " + e.getMessage());
                    }
                    
                    // ✅ 최종 가격 결정
                    double finalPrice = (discountPrice > 0) ? discountPrice : originalPrice;
                    
                    // ✅ 디버깅 로그
                    System.out.println("💰 [디버깅] 원가: " + originalPrice);
                    System.out.println("💰 [디버깅] 할인 가격: " + discountPrice);
                    System.out.println("💰 [디버깅] 최종 가격: " + finalPrice);

                    // ✅ 대표 이미지 크롤링
                    Locator imageLocator = detailPage.locator("div.prod-image img").first();
                    String imageUrl = imageLocator.isVisible() ? imageLocator.getAttribute("src") : "";

                    // ✅ 추가 이미지 크롤링
                    List<String> additionalImages = new ArrayList<>();
                    for (Locator imgLocator : detailPage.locator("div.prod-image img").all()) {
                        if (imgLocator.isVisible()) {
                            String imgSrc = imgLocator.getAttribute("src");
                            if (imgSrc != null && !imgSrc.trim().isEmpty() && !imgSrc.equals(imageUrl)) {
                                additionalImages.add(imgSrc);
                            }
                        }
                    }
                    // // ✅ 기본 옵션 리스트 생성
                    // List<OptionDto> optionList = new ArrayList<>();
                    // Set<String> optionSet = new HashSet<>(); // 중복 방지

                    // // ✅ 옵션 요소 탐색 (optionWrapper 내부)
                    // Locator optionWrapper = detailPage.locator("#optionWrapper");

                    // if (optionWrapper.count() > 0) {
                    //     Locator optionLocator = optionWrapper.locator("li");
                    //     if (optionLocator.count() > 0) {
                    //         for (Locator option : optionLocator.all()) {
                    //             String optionText = option.textContent().trim();
                    //             if (!optionText.isEmpty() && !optionSet.contains(optionText)) {
                    //                 optionSet.add(optionText);
                    //                 optionList.add(new OptionDto("OPTION", optionText));
                    //             }
                    //         }
                    //     }
                    // }

                    // // ✅ 옵션이 없거나 크롤링 실패 시 기본 옵션 추가
                    // if (optionList.isEmpty()) {
                    //     System.out.println("⚠️ [옵션 없음] 기본 옵션으로 설정");
                    //     optionList.add(new OptionDto("기본 옵션", "단일 상품"));
                    // }

                    // // ✅ 크롤링된 옵션 출력 (디버깅)
                    // for (OptionDto option : optionList) {
                    //     System.out.println("🛠 [옵션 크롤링] " + option);
                    // }

                    

                    // ✅ 기본 옵션 리스트 생성
                    List<OptionDto> optionList = new ArrayList<>();
                    Set<String> optionSet = new HashSet<>(); // 중복 방지

                    

                    // ✅ `optionWrapper` 내부 옵션 체크
                    Locator optionWrapper = detailPage.locator("#optionWrapper");
                    

                    // ✅ 옵션이 있는지 먼저 확인
                    if (optionWrapper.count() > 0) {
                        Locator optionLocator = optionWrapper.locator("li");
                        if (optionLocator.count() > 0) {
                            for (Locator option : optionLocator.all()) {
                                String optionText = option.textContent().trim();
                                if (!optionText.isEmpty() && !optionSet.contains(optionText)) {
                                    optionSet.add(optionText);
                                    optionList.add(new OptionDto("OPTION", optionText));
                                }
                            }
                        }
                    }

                    // ✅ `optionWrapper`가 존재하는지 먼저 확인
                    // ✅ 옵션 요소가 존재하는지 확인한 후 `waitForSelector` 실행
                    if (detailPage.locator("div.prod-option, ul.Image_Select__items, div.tab-selector__tab").count() > 0) {
                        System.out.println("✅ 옵션 요소 감지됨. 크롤링 대기...");
                        detailPage.waitForSelector(
                            "div.prod-option, ul.Image_Select__items, div.tab-selector__tab",
                            new Page.WaitForSelectorOptions().setTimeout(5000) // ✅ 대기 시간 단축
                        );
                    } else {
                        System.out.println("⚠️ [경고] 옵션 요소 없음. 기본 옵션 처리.");
                    }

                        // ✅ 옵션 요소가 로드될 때까지 대기
                        try {
                            detailPage.waitForSelector(
                                "div.prod-option, ul.Image_Select__items, div.tab-selector__tab",
                                new Page.WaitForSelectorOptions().setTimeout(15000)
                            );
                        } catch (TimeoutError e) {
                            System.out.println("⚠️ [옵션 없음] 해당 상품은 옵션이 없습니다. 기본 옵션 처리.");
                        }

                        // ✅ 옵션 크롤링을 위한 `Locator` 리스트 생성
                        List<Locator> optionLocators = Arrays.asList(
                            detailPage.locator("ul.prod-option__item li"),
                            detailPage.locator("div.Dropdown-Select.prod-option__item"),
                            detailPage.locator("div.prod-option__selected-container button")
                        );

                        // ✅ 각 옵션을 탐색하여 중복 없이 추가
                        for (Locator locator : optionLocators) {
                            if (locator.count() > 0) {
                                for (Locator option : locator.all()) {
                                    String optionValue = option.textContent();
                                    if (optionValue == null || optionValue.trim().isEmpty()) {
                                        optionValue = "단일 상품"; // 기본값 설정
                                    }
                                    if (!optionSet.contains(optionValue.trim())) {
                                        optionSet.add(optionValue.trim());
                                        optionList.add(new OptionDto("OPTION", optionValue.trim()));
                                    }
                                }
                            }
                        }
                        // ✅ 드롭다운 옵션 크롤링
                        Locator dropdownOptions = detailPage.locator("ul.prod-option__item li");
                        for (Locator option : dropdownOptions.all()) {
                            String optionText = option.textContent().trim();
                            if (!optionText.isEmpty() && !optionSet.contains(optionText)) {
                                optionSet.add(optionText);
                                optionList.add(new OptionDto("드롭다운 옵션", optionText));
                            }
                        }

                        // ✅ 이미지 옵션 크롤링 (추가)
                        Locator imageOptions = detailPage.locator("ul.Image_Select__items li");
                        for (Locator option : imageOptions.all()) {
                            String optionValue = option.getAttribute("data-thumbnail-image-url");
                            if (optionValue == null) {
                                optionValue = option.getAttribute("data-origin-image-url");
                            }
                            if (optionValue != null && !optionSet.contains(optionValue)) {
                                optionSet.add(optionValue);
                                optionList.add(new OptionDto("이미지 옵션", optionValue));
                            }
                        }
                        // ✅ 탭 옵션 크롤링 (추가)
                        Locator tabContainer = detailPage.locator("div.tab-selector-container");
                        if (tabContainer.count() > 0) {
                            Locator tabOptions = tabContainer.locator("div.tab-selector__tab");
                            for (Locator option : tabOptions.all()) {
                                String optionText = option.locator("div.tab-selector__tab-title").textContent().trim();
                                if (!optionSet.contains(optionText)) {
                                    optionSet.add(optionText);
                                    optionList.add(new OptionDto("탭 옵션", optionText));
                                }
                            }
                        }
                        // ✅ 표 형식 옵션 크롤링 (추가)
                        Locator optionContainer = detailPage.locator("div.prod-option");
                        if (optionContainer.count() > 0) {
                            List<Locator> optionRows = optionContainer.locator("tr").all();
                            for (Locator row : optionRows) {
                                String optionTitle = row.locator("span.title").textContent().trim();
                                String optionValue = row.locator("span.value").textContent().trim();
                                if (!optionTitle.isEmpty() && !optionValue.isEmpty() && !optionSet.contains(optionValue)) {
                                    optionSet.add(optionValue);
                                    optionList.add(new OptionDto(optionTitle, optionValue));
                                }
                            }
                        }
                        // ✅ 추가 상품 옵션 (번들 옵션) 크롤링 (추가)
                        Locator bundleOptions = detailPage.locator("div.bundle-option");
                        for (Locator option : bundleOptions.all()) {
                            String bundleText = option.textContent().trim();
                            if (!bundleText.isEmpty() && !optionSet.contains(bundleText)) {
                                optionSet.add(bundleText);
                                optionList.add(new OptionDto("번들 옵션", bundleText));
                            }
                        }

                        // ✅ 라디오 버튼 옵션 크롤링 (추가)
                        Locator radioOptions = detailPage.locator("input[type='radio']");
                        for (Locator option : radioOptions.all()) {
                            String optionText = option.getAttribute("value");
                            if (optionText != null && !optionSet.contains(optionText)) {
                                optionSet.add(optionText);
                                optionList.add(new OptionDto("라디오 버튼 옵션", optionText));
                            }
                        }

                        // ✅ 셀렉트 박스 옵션 크롤링 (추가)
                        Locator selectOptions = detailPage.locator("select option");
                        for (Locator option : selectOptions.all()) {
                            String optionText = option.textContent().trim();
                            if (!optionText.isEmpty() && !optionSet.contains(optionText)) {
                                optionSet.add(optionText);
                                optionList.add(new OptionDto("셀렉트 옵션", optionText));
                            }
                        }

                        // ✅ 텍스트 입력형 옵션 크롤링 (개선)
                        Locator textInputOptions = detailPage.locator("input[type='text']");
                        for (Locator option : textInputOptions.all()) {
                            String placeholder = option.getAttribute("placeholder");
                            String value = option.getAttribute("value"); // ✅ 입력값도 확인
                            if ((placeholder != null && !optionSet.contains(placeholder)) || 
                                (value != null && !optionSet.contains(value))) {
                                
                                optionSet.add(placeholder != null ? placeholder : value);
                                optionList.add(new OptionDto("텍스트 입력 옵션", placeholder != null ? placeholder : value));
                            }
                        }

                        // ✅ 옵션별 가격 변동 크롤링 (옵션 값과 함께 저장)
                        Locator priceChangeOptions = detailPage.locator("span.price-change");
                        for (Locator option : priceChangeOptions.all()) {
                            String priceText = option.textContent().trim();
                            Locator parentOption = option.locator(".."); // 부모 요소에서 옵션 이름 찾기
                            String optionName = parentOption.textContent().trim();
                            
                            if (!priceText.isEmpty() && !optionSet.contains(priceText)) {
                                String finalText = optionName + " (" + priceText + ")";
                                optionSet.add(finalText);
                                optionList.add(new OptionDto("옵션별 가격 변동", finalText));
                            }
                        }

                        // ✅ 옵션이 없는 경우 기본 옵션 추가
                        if (optionList.isEmpty()) {
                            optionList.add(new OptionDto("기본 옵션", "단일 상품"));
                        }

                        // ✅ 크롤링된 옵션 출력
                        for (OptionDto option : optionList) {
                            System.out.println("🛠 [옵션 크롤링] " + option);
                        }


                    // ✅ 상품 데이터 저장
                    ProductDto productDto = ProductDto.builder()
                            .name(name)
                            .price(finalPrice)
                            .stockQuantity(10)
                            .categoryId(category.getCategoryId())
                            .imageUrl(imageUrl)
                            .thumbnailImageUrl(imageUrl)
                            .detailUrl(detailUrl)
                            .options(optionList)
                            .additionalImages(additionalImages)
                            .build();

                    Product savedProduct = productService.saveProduct(productDto);
                    if (savedProduct == null) {
                        System.out.println("🚨 [saveProduct] 상품 저장 실패로 크롤링 종료!");
                        return;
                    }

                    // ✅ 할인 정보 저장
                    if (originalPrice > discountPrice) {
                        discountService.saveDiscount(savedProduct, "PERCENT", (originalPrice - discountPrice) / originalPrice * 100);
                    }

                // ✅ finally 블록을 올바르게 정리
                try {
                    if (detailPage == null || detailPage.isClosed()) {
                        System.out.println("🚨 [경고] detailPage가 null이거나 닫혀 있습니다. 새 페이지를 생성합니다.");
                        detailPage = context.newPage();
                    }
                } finally {
                    detailPage.close();
                }

                count++;
            }
            context.close();
            browser.close();
        }
    }
}