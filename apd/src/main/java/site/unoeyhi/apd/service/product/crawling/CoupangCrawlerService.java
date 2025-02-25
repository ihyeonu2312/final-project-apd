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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                "--disable-gpu"
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
                            detailPage.navigate(detailUrl, new Page.NavigateOptions()
                                .setTimeout(6000) // ✅ Timeout 120초로 증가
                                .setWaitUntil(WaitUntilState.LOAD) // ✅ networkidle → domcontentloaded 변경
                            );
                
                            // ✅ 페이지 로드 후 3초 대기 (네트워크 속도 조절)
                            detailPage.waitForTimeout(3000);
                
                            // ✅ 정상 로딩 확인
                            if (!detailPage.url().equals("about:blank") && detailPage.locator("body").count() > 0) {
                                success = true;
                            }
                        } catch (PlaywrightException e) {
                            System.out.println("🚨 [경고] 페이지 로드 실패: " + e.getMessage());
                        }
                        retryCount++;
                    }
                
                    if (!success) {
                        System.out.println("🚨 [실패] 상품 페이지 로드 실패로 크롤링 건너뜀: " + detailUrl);
                        detailPage.close();
                        return;
                    }
                
                    // ✅ **상세 페이지 크롤링 유지**
                    System.out.println("✅ [성공] 상세 페이지 크롤링 시작: " + detailUrl);

                    // ✅ 가격 크롤링
                    // 기존 선택자
                    Locator originalPriceLocator = detailPage.locator("del.base-price");
                    Locator discountPriceLocator = detailPage.locator("del.base-price + span");

                    // ✅ 새로운 선택자 (백업)
                    Locator newOriginalPriceLocator = detailPage.locator("span.origin-price");  // 원가
                    Locator newDiscountPriceLocator = detailPage.locator("span.total-price");   // 할인가
                    Locator salePriceLocator = detailPage.locator("span.final-price"); // 최종 가격 (이게 있을 수도 있음)

                    // ✅ 가격 파싱
                    String originalPriceText = originalPriceLocator.count() > 0 ? originalPriceLocator.textContent().trim() : 
                                                newOriginalPriceLocator.count() > 0 ? newOriginalPriceLocator.textContent().trim() : "";
                    String discountPriceText = discountPriceLocator.count() > 0 ? discountPriceLocator.textContent().trim() :
                                                newDiscountPriceLocator.count() > 0 ? newDiscountPriceLocator.textContent().trim() : 
                                                salePriceLocator.count() > 0 ? salePriceLocator.textContent().trim() : "";

                    // ✅ 가격 값 변환
                    double originalPrice = 0.0;
                    double discountPrice = 0.0;
                    try {
                        if (!originalPriceText.isEmpty() && originalPriceText.matches(".*\\d.*")) {  
                            originalPrice = Double.parseDouble(originalPriceText.replaceAll("[^0-9.]", ""));
                        }
                        if (!discountPriceText.isEmpty() && discountPriceText.matches(".*\\d.*")) {  
                            discountPrice = Double.parseDouble(discountPriceText.replaceAll("[^0-9.]", ""));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("🚨 [오류] 가격 변환 실패: " + e.getMessage());
                    }

                    // ✅ 최종 가격 결정
                    double finalPrice = (discountPrice > 0) ? discountPrice : originalPrice;

                    // ✅ 디버깅 로그 추가
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

                    // ✅ 옵션 크롤링
                    List<OptionDto> optionList = new ArrayList<>();
                    Locator optionWrapperLocator = detailPage.locator("div#optionWrapper");

                    if (optionWrapperLocator.count() > 0 && optionWrapperLocator.isVisible()) {
                        List<String> optionValues = detailPage.locator("ul.prod-option__item li").allInnerTexts();
                        for (String optionValue : optionValues) {
                            optionValue = optionValue.trim();
                            if (!optionValue.isEmpty()) {
                                optionList.add(new OptionDto("DEFAULT", optionValue));
                            }
                        }
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
                }
                count++;
            }
            browser.close();
        }
    }
}
