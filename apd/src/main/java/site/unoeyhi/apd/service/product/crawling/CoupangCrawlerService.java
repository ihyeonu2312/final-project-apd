package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
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
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36") // 일반 브라우저로 인식
            .setExtraHTTPHeaders(Map.of(
                "Accept-Language", "ko-KR,ko;q=0.9",
                "Referer", "https://www.coupang.com/",
                "X-Forwarded-For", "220.95.91.1" // ✅ IP 우회 효과
            ))
        );
        
            context.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => false })");
        
            Page currentPage = context.newPage();
            currentPage.navigate(categoryUrl, new Page.NavigateOptions().setTimeout(120000).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            List<ElementHandle> productElements = currentPage.querySelectorAll("li.baby-product.renew-badge");
            if (productElements.isEmpty()) {
                System.out.println("🚨 상품 없음 (선택자 확인 필요)");
                return;
            }

            int count = 0;
            for (ElementHandle productElement : productElements) {
                if (count >= 10) break;

                ElementHandle nameElement = productElement.querySelector("div.name");
                String name = (nameElement != null) ? nameElement.innerText().trim() : "알 수 없음";
                System.out.println("🏷️ [디버깅] 상품명: " + name);

                ElementHandle linkElement = productElement.querySelector("a.baby-product-link");
                String detailUrl = (linkElement != null) ? "https://www.coupang.com" + linkElement.getAttribute("href") : "";
                System.out.println("🔍 [디버깅] 상품 상세 URL: " + detailUrl);

                Page detailPage = context.newPage();

                // context.setExtraHTTPHeaders(headers);
                
                // ✅ 상세 페이지 크롤링
                try {
                    int retryCount = 0;
                    boolean success = false;
                
                    while (!success && retryCount < 3) {
                        try {
                            System.out.println("🔄 [재시도 " + (retryCount + 1) + "] 상품 페이지 로딩 중: " + detailUrl);
                
                            // ✅ 페이지가 닫혀있으면 다시 생성
                            if (detailPage.isClosed()) { 
                                detailPage = context.newPage();
                            }
                
                            // ✅ User-Agent 및 WebDriver 조작 (차단 방지)
                            context.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => undefined })");
                
                            // ✅ 페이지 로드
                            detailPage.navigate(detailUrl, new Page.NavigateOptions()
                                .setTimeout(180000)  // ✅ 타임아웃 증가
                                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                            );
                
                            // ✅ iframe 감지 후 mainFrame 전환
                            List<Frame> frames = detailPage.frames();
                            for (Frame frame : frames) {
                                if (frame.url().contains("coupang.com")) {
                                    System.out.println("📌 [경고] iframe 감지됨. mainFrame으로 전환 시도...");
                                    detailPage = frame.page();
                                    break;
                                }
                            }
                
                            // ✅ `about:blank` 상태 확인
                            if (detailPage.url().equals("about:blank") || detailPage.locator("body").count() == 0) {
                                throw new PlaywrightException("🚨 [경고] 페이지가 정상적으로 로드되지 않음 (about:blank)!");
                            }
                
                            // ✅ 정상 로딩 완료
                            success = true;
                            System.out.println("✅ [성공] 상세 페이지 로딩 완료: " + detailPage.url());
                
                        } catch (PlaywrightException e) {
                            System.out.println("🚨 [경고] 페이지 로드 실패: " + e.getMessage());
                
                            retryCount++;
                            detailPage.waitForTimeout(3000); // ✅ 3초 대기 후 재시도
                
                            if (retryCount >= 3) {
                                System.out.println("🚨 [실패] 상품 페이지 로드 실패로 크롤링 건너뜀: " + detailUrl);
                                return;
                            }
                        }
                    }
                
                    // ✅ **상세 페이지 크롤링 유지**
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

                    // ✅ 기본 옵션 리스트 생성
                    List<OptionDto> optionList = new ArrayList<>();
                    Set<String> optionSet = new HashSet<>(); // 중복 방지

                    // ✅ `optionWrapper` 내부 옵션 체크
                    Locator optionWrapper = detailPage.locator("#optionWrapper");
                    if (optionWrapper.count() > 0 && !optionWrapper.getAttribute("class").contains("no-option")) {
                        System.out.println("🔍 [옵션 감지됨] 옵션 분석 시작...");

                        // ✅ 옵션 요소가 로드될 때까지 대기
                        detailPage.waitForSelector(
                            "div.prod-option, ul.Image_Select__items, div.tab-selector__tab",
                            new Page.WaitForSelectorOptions().setTimeout(5000)
                        );

                        // ✅ 옵션 크롤링을 위한 `Locator` 리스트 생성
                        List<Locator> optionLocators = Arrays.asList(
                            detailPage.locator("ul.prod-option__item li"),  // 기본 옵션
                            detailPage.locator("div.Dropdown-Select.prod-option__item"), // 드롭다운 방식
                            detailPage.locator("div.prod-option__selected-container button") // 선택된 옵션 방식
                        );

                        // ✅ 각 옵션을 탐색하여 중복 없이 추가
                        for (Locator locator : optionLocators) {
                            if (locator.count() > 0) {
                                for (Locator option : locator.all()) {
                                    String optionValue = option.textContent().trim();
                                    if (!optionSet.contains(optionValue)) {
                                        optionSet.add(optionValue);
                                        optionList.add(new OptionDto("OPTION", optionValue));
                                    }
                                }
                            }
                        }

                        // ✅ 이미지 옵션 탐색
                        Locator imageOptions = detailPage.locator("ul.Image_Select__items li");
                        if (imageOptions.count() > 0) {
                            for (Locator option : imageOptions.all()) {
                                String optionValue = option.getAttribute("data-thumbnail-image-url");
                                if (optionValue == null) {
                                    optionValue = option.getAttribute("data-origin-image-url");
                                }
                                if (optionValue != null && !optionSet.contains(optionValue.trim())) {
                                    optionSet.add(optionValue.trim());
                                    optionList.add(new OptionDto("IMAGE", optionValue.trim()));
                                }
                            }
                        }

                        // ✅ `tab-selector` 방식 옵션 탐색
                        Locator tabOptions = detailPage.locator("div.tab-selector__tab");
                        if (tabOptions.count() > 0) {
                            for (Locator option : tabOptions.all()) {
                                String optionValue = option.getAttribute("data-id"); // 옵션 ID
                                Locator imageOption = option.locator("img.tab-selector__tab-image");
                                String optionImage = imageOption.count() > 0 ? imageOption.getAttribute("src") : null; // 옵션 이미지

                                if (optionValue != null && !optionSet.contains(optionValue.trim())) {
                                    optionSet.add(optionValue.trim());
                                    if (optionImage != null) {
                                        optionList.add(new OptionDto("TAB", optionValue.trim(), optionImage));
                                    } else {
                                        optionList.add(new OptionDto("TAB", optionValue.trim()));
                                    }
                                }
                            }
                        }

                        // ✅ `prod-option` 기반 옵션 탐색 (테이블 형식)
                        Locator optionContainer = detailPage.locator("div.prod-option");
                        if (optionContainer.count() > 0) {
                            List<Locator> optionRows = optionContainer.locator("tr").all();
                            for (Locator row : optionRows) {
                                Locator titleLocator = row.locator("span.title");
                                Locator valueLocator = row.locator("span.value");

                                String optionTitle = titleLocator.count() > 0 ? titleLocator.textContent().trim() : "";
                                String optionValue = valueLocator.count() > 0 ? valueLocator.textContent().trim() : "";

                                if (!optionTitle.isEmpty() && !optionValue.isEmpty() && !optionSet.contains(optionValue)) {
                                    optionSet.add(optionValue);
                                    System.out.println("🛠 [옵션 크롤링] " + optionTitle + ": " + optionValue);
                                    optionList.add(new OptionDto(optionTitle, optionValue));
                                }
                            }
                        }
                    }

                    // ✅ 옵션이 없는 경우 기본값 추가
                    if (optionList.isEmpty()) {
                        System.out.println("⚠️ [옵션 없음] 기본값으로 설정");
                        optionList.add(new OptionDto("기본 옵션", "단일 상품"));
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

                } catch (Exception e) {
                    System.out.println("🚨 [크롤링 오류] " + e.getMessage());
                }finally {
                    detailPage.close(); // ✅ finally 블록에서 페이지 닫기
                }
                count++;
            }
            browser.close();
        }
    }
}
