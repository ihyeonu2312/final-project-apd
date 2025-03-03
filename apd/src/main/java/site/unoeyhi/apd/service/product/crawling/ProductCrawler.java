package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.dto.product.OptionDto;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.service.product.ProductService;
import software.amazon.awssdk.services.dynamodb.endpoints.internal.Value.Str;
import site.unoeyhi.apd.service.product.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service 
public class ProductCrawler {

    private final ProductService productService;
    private final DiscountService discountService;

    @Autowired
    public ProductCrawler(ProductService productService, DiscountService discountService) {
        this.productService = productService;
        this.discountService = discountService;
    }

    /**
     * ✅ 상품 상세 정보 크롤링
     */
    public void crawlProductDetail(BrowserContext context, String detailUrl) {
        System.out.println("🚀 [crawlProductDetail] 상세 상품 크롤링 시작: " + detailUrl);
    
        Page detailPage = openDetailPage(context, detailUrl);
        if (detailPage == null) {
            System.out.println("🚨 [오류] 상세 페이지를 열 수 없어 크롤링 건너뜀.");
            return;
        }
    
        // ✅ 상품명 크롤링 (locator 사용)
        Locator titleLocator = detailPage.locator("h2.prod-buy-header__title, span.prod-buy-header__product-title");

        // ✅ `productTitle`을 미리 선언
        String productTitle = null;

        if (titleLocator.count() > 0 && titleLocator.isVisible()) {
            productTitle = titleLocator.textContent().trim();
            System.out.println("✅ [DEBUG] 상품 제목 크롤링 결과 (locator 방식): " + productTitle);
        } else {
            System.out.println("🚨 [오류] 상품 제목을 찾을 수 없어 크롤링 건너뜀.");
            return; // 상품 제목이 없으면 크롤링 중단
        }

        System.out.println("🛒 [상품명] " + productTitle);
    
        // ✅ 가격 크롤링 (String 캐스팅)
        String originalPriceStr = detailPage.locator("span.origin-price").count() > 0 
            ? detailPage.locator("span.origin-price").textContent() 
            : "0";

        String discountPriceStr = detailPage.locator("span.discount-price").count() > 0 
            ? detailPage.locator("span.discount-price").textContent() 
            : "0";


        double originalPrice = parsePrice(originalPriceStr);
        double discountPrice = parsePrice(discountPriceStr);
        double finalPrice = (discountPrice > 0) ? discountPrice : originalPrice;
    
        System.out.println("💰 [가격] 원가: " + originalPrice + " | 할인 가격: " + discountPrice + " | 최종 가격: " + finalPrice);
    
        // ✅ 이미지 크롤링 (String 캐스팅)
        String imageUrl = detailPage.locator("div.prod-image img").count() > 0 
        ? detailPage.locator("div.prod-image img").first().getAttribute("src") 
        : "";

    
        // ✅ 추가 이미지 크롤링 (List<String>으로 변환)
        List<String> additionalImages = (List<String>) detailPage.evaluate("() => Array.from(document.querySelectorAll('div.prod-image img')).map(img => img.src)");

    
        // ✅ 옵션 크롤링
        List<OptionDto> optionList = extractOptions(detailPage);
    
        System.out.println("🛠 [crawlProductDetail] 상품 저장 시작: " + productTitle);
    
        // ✅ 상품 저장
        ProductDto productDto = ProductDto.builder()
                .name(productTitle)
                .price(finalPrice)
                .stockQuantity(10)
                .imageUrl(imageUrl)
                .thumbnailImageUrl(imageUrl)
                .detailUrl(detailUrl)
                .options(optionList)
                .additionalImages(additionalImages)
                .build();
    
        Product savedProduct = productService.saveProduct(productDto);
        if (savedProduct == null) {
            System.out.println("🚨 [상품 저장 실패] 크롤링 종료!");
            return;
        }
    
        System.out.println("✅ [상품 저장 성공] ID: " + savedProduct.getProductId() + " | 이름: " + savedProduct.getName());
    
        detailPage.close();
    }
    
    // ✅ 가격 문자열을 숫자로 변환하는 유틸리티 메서드
    private double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0.0;
        return Double.parseDouble(priceStr.replaceAll("[^0-9]", ""));
    }
    
    //상품 상세
    private Page openDetailPage(BrowserContext context, String detailUrl) {
        Page detailPage = context.newPage();
        int retryCount = 0;
        boolean success = false;
    
        while (!success && retryCount < 3) {
            try {
                System.out.println("🔄 [상품 상세 페이지 로딩] " + detailUrl);
                detailPage.navigate(detailUrl, new Page.NavigateOptions()
                    .setTimeout(90000)
                    .setWaitUntil(WaitUntilState.LOAD)  // ✅ 네트워크 완료 대기
                );
    
                // ✅ 현재 URL 및 타이틀 확인
                System.out.println("✅ [DEBUG] 페이지 로딩 완료: " + detailPage.url());
                System.out.println("✅ [DEBUG] 페이지 타이틀: " + detailPage.title());
    
                // ✅ 페이지가 `about:blank` 상태이면 실패 처리
                if (detailPage.url().equals("about:blank") || detailPage.title().isEmpty()) {
                    System.out.println("🚨 [경고] `about:blank` 감지됨! 페이지가 제대로 열리지 않음.");
                    detailPage.waitForTimeout(3000);
                    detailPage.reload();
                    continue;
                }
    
                // ✅ Playwright 봇 감지 우회 설정
                detailPage.evaluate("() => { Object.defineProperty(navigator, 'webdriver', { get: () => false }); }");
    
                // ✅ 페이지 끝까지 스크롤 (Lazy Loading 대응)
                for (int i = 0; i < 6; i++) {
                    detailPage.mouse().wheel(0, 600);
                    detailPage.waitForTimeout(1500);
                }
    
                // ✅ 페이지가 완전히 로드될 때까지 대기
                detailPage.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(60000));
    
                // ✅ 상품 제목 로드 확인
                Locator titleLocator = detailPage.locator("h2.prod-buy-header__title, span.prod-buy-header__product-title");
                System.out.println("✅ [DEBUG] 상품 제목 요소 개수: " + titleLocator.count());
    
                if (titleLocator.isVisible()) {
                    System.out.println("✅ [DEBUG] 상품 제목 감지됨: " + titleLocator.textContent().trim());
                    success = true;
                } else {
                    throw new Exception("상품 제목 감지 실패");
                }
    
            } catch (Exception e) {
                retryCount++;
                System.out.println("🚨 [재시도 " + retryCount + "] 페이지 로딩 실패, 다시 시도...");
                detailPage.reload();
            }
        }
    
        if (!success) {
            System.out.println("🚨 [실패] 상세 페이지 로드 불가: " + detailUrl);
            detailPage.close();
            return null;
        }
    
        return detailPage;
    }
    


    /**
     * ✅ 상품 제목 크롤링 (여러 요소 대응)
     */
    private String getProductTitle(Page page) {
        Locator titleLocator = page.locator("h2.prod-buy-header__title, span.prod-buy-header__product-title");

        try {
            titleLocator.waitFor(new Locator.WaitForOptions().setTimeout(60000)); // ✅ 기존 50초 → 60초 증가
    
            if (titleLocator.isVisible()) {
                return titleLocator.textContent().trim();
            } else {
                throw new Exception("상품 제목이 표시되지 않음");
            }
        } catch (Exception e) {
            System.out.println("🚨 [경고] 상품 제목 감지 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * ✅ 가격 크롤링 메서드
     */
    private double extractPrice(Page page, String selector) {
        Locator priceLocator = page.locator(selector).first();
        String priceText = priceLocator.count() > 0 ? priceLocator.textContent().replaceAll("[^0-9]", "") : "";
        return priceText.isEmpty() ? 0.0 : Double.parseDouble(priceText);
    }

    /**
     * ✅ 추가 이미지 크롤링
     */
    private List<String> extractAdditionalImages(Page page) {
        List<String> images = new ArrayList<>();
        for (Locator imgLocator : page.locator("div.prod-image img").all()) {
            String imgSrc = imgLocator.getAttribute("src");
            if (imgSrc != null && !imgSrc.trim().isEmpty()) {
                images.add(imgSrc);
            }
        }
        return images;
    }

    /**
     * ✅ 옵션 크롤링
     */
    private List<OptionDto> extractOptions(Page page) {
        List<OptionDto> optionList = new ArrayList<>();
        Set<String> optionSet = new HashSet<>();

        for (Locator option : page.locator("ul.prod-option__item li").all()) {
            String optionText = option.textContent().trim();
            if (!optionText.isEmpty() && optionSet.add(optionText)) {
                optionList.add(new OptionDto("드롭다운 옵션", optionText));
            }
        }

        if (optionList.isEmpty()) {
            optionList.add(new OptionDto("기본 옵션", "단일 상품"));
        }

        return optionList;
    }
}
