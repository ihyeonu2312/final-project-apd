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
import java.util.Map;
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
    
        try {
            // ✅ 상품명 크롤링
            Locator titleLocator = detailPage.locator("h1.prod-buy-header__title");
            String productTitle = titleLocator.all().get(0).textContent().trim();
            System.out.println("🛒 [상품명] " + productTitle);
    
            // ✅ 가격 크롤링
            double originalPrice = extractPrice(detailPage, "span.origin-price");
            double discountPrice = extractPrice(detailPage, "span.discount-price");
            double finalPrice = (discountPrice > 0) ? discountPrice : originalPrice;
    
            System.out.println("💰 [가격] 원가: " + originalPrice + " | 할인 가격: " + discountPrice + " | 최종 가격: " + finalPrice);
    
            // ✅ 이미지 크롤링
            String imageUrl = detailPage.locator("div.prod-image img").count() > 0 
                ? detailPage.locator("div.prod-image img").first().getAttribute("src") 
                : "https://via.placeholder.com/300";
    
            // ✅ 옵션 크롤링
            List<OptionDto> optionList = extractOptions(detailPage);
    
            // ✅ 상품 저장 확인 로그
            System.out.println("🛠 [DEBUG] 저장할 상품 데이터:");
            System.out.println("   🔹 이름: " + productTitle);
            System.out.println("   🔹 가격: " + finalPrice);
            System.out.println("   🔹 이미지: " + imageUrl);
            System.out.println("   🔹 옵션 개수: " + optionList.size());
    
            // ✅ 상품 저장
            ProductDto productDto = ProductDto.builder()
                    .name(productTitle)
                    .categoryId(1L) // 여기에 올바른 categoryId 넣어주면 됩니다.
                    .price(finalPrice)
                    .stockQuantity(10)
                    .imageUrl(imageUrl)
                    .thumbnailImageUrl(imageUrl)
                    .detailUrl(detailUrl)
                    .options(optionList)
                    .build();
    
            Product savedProduct = productService.saveProduct(productDto);
            if (savedProduct == null) {
                System.out.println("🚨 [상품 저장 실패] 크롤링 종료!");
            } else {
                System.out.println("✅ [상품 저장 성공] ID: " + savedProduct.getProductId() + " | 이름: " + savedProduct.getName());
            }
    
        } catch (Exception e) {
            System.out.println("🚨 [오류 발생] " + e.getMessage());
        } finally {
            detailPage.close();
        }
    }
    
    
    
    /**
     * ✅ 카테고리 내 모든 상품을 크롤링하고 자동 저장
     */
    public List<ProductDto> crawlAllProducts(BrowserContext context, String categoryUrl) {
        System.out.println("🚀 [crawlAllProducts] 카테고리 상품 크롤링 시작: " + categoryUrl);
    
        Page page = context.newPage();
        page.navigate(categoryUrl, new Page.NavigateOptions().setTimeout(60000).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    
        // ✅ 상품 리스트가 로딩될 때까지 대기
        page.waitForTimeout(3000);
        page.waitForSelector("li.baby-product.renew-badge", new Page.WaitForSelectorOptions().setTimeout(10000));
    
        // ✅ 상품 개수 확인 (디버깅용)
        int productCount = page.locator("li.baby-product.renew-badge").count();
        System.out.println("📦 [DEBUG] Playwright가 감지한 상품 개수: " + productCount);
    
        // ✅ 상품이 없는 경우 처리
        if (productCount == 0) {
            System.out.println("🚨 [경고] 상품이 없음! 페이지 구조 변경 가능성 있음.");
            System.out.println("📌 현재 페이지 HTML: " + page.content());  
            return new ArrayList<>();
        }
    
        // ✅ 카테고리 페이지에서 상품 URL 추출
        List<String> productUrls = new ArrayList<>();
        List<ElementHandle> productElements = page.querySelectorAll("li.baby-product.renew-badge");
    
        for (ElementHandle productElement : productElements) {
            try {
                String productId = productElement.getAttribute("data-product-id");
                if (productId != null && !productId.trim().isEmpty()) {
                    String productUrl = "https://www.coupang.com/vp/products/" + productId;
                    productUrls.add(productUrl);
                    System.out.println("🔗 [상품 URL 추가] " + productUrl);
                }
            } catch (Exception e) {
                System.out.println("🚨 [오류 발생] 상품 URL 추출 중 문제 발생: " + e.getMessage());
            }
        }
    
        System.out.println("📦 [crawlAllProducts] 총 상품 개수: " + productUrls.size());
    
        // ✅ 상품 상세 크롤링 & 자동 저장
        for (String productUrl : productUrls) {
            System.out.println("🛠 [crawlAllProducts] 상품 상세 크롤링 호출: " + productUrl);
            crawlProductDetail(context, productUrl);
        }
    
        page.close();
        return new ArrayList<>();
    }
    
    //상품 상세
    private Page openDetailPage(BrowserContext context, String detailUrl) {
        Page detailPage = null;
        int retryCount = 0;
        boolean success = false;
    
        while (!success && retryCount < 3) {
            try {
                if (detailPage != null) {
                    detailPage.close(); // ✅ 기존 페이지 닫고 새로 열기
                }
                detailPage = context.newPage();
                System.out.println("🔄 [상품 상세 페이지 로딩 시도] (" + (retryCount + 1) + ") " + detailUrl);
    
                // ✅ 페이지 이동
                detailPage.navigate(detailUrl);
                detailPage.waitForTimeout(3000);
    
    
                // ✅ 상품명 확인
                Locator titleLocator = detailPage.locator("h1.prod-buy-header__title");
                if (titleLocator.count() > 0) {
                    success = true;
                    System.out.println("✅ [상품 페이지 로딩 완료] 제목: " + titleLocator.all().get(0).textContent().trim());
                } else {
                    throw new Exception("상품 제목 감지 실패");
                }
    
            } catch (Exception e) {
                retryCount++;
                System.out.println("🚨 [재시도 " + retryCount + "] 페이지 로딩 실패, 다시 시도...");
            }
        }
    
        if (!success) {
            System.out.println("🚨 [실패] 상세 페이지 로드 불가: " + detailUrl);
            if (detailPage != null) detailPage.close();
            return null;
        }
    
        return detailPage;
    }
    
    
    
    
    
    
    

    // /**
    //  * ✅ 상품 제목 크롤링 (여러 요소 대응)
    //  */
    // private String getProductTitle(Page page) {
    //     Locator titleLocator = page.locator("h2.prod-buy-header__title, span.prod-buy-header__product-title");

    //     try {
    //         titleLocator.waitFor(new Locator.WaitForOptions().setTimeout(60000)); // ✅ 기존 50초 → 60초 증가
    
    //         if (titleLocator.isVisible()) {
    //             return titleLocator.textContent().trim();
    //         } else {
    //             throw new Exception("상품 제목이 표시되지 않음");
    //         }
    //     } catch (Exception e) {
    //         System.out.println("🚨 [경고] 상품 제목 감지 실패: " + e.getMessage());
    //         return null;
    //     }
    // }

    /**
     * ✅ 가격 크롤링 메서드
     */
    private double extractPrice(Page page, String selector) {
        Locator priceLocator = page.locator(selector).first();
        if (priceLocator.count() == 0) {
            return 0.0;  // ✅ 가격 정보가 없으면 0 반환
        }
    
        try {
            String priceText = priceLocator.textContent().replaceAll("[^0-9,.]", "").trim();
            if (priceText.contains(",")) {  // ✅ 콤마(,)가 포함되어 있으면 제거
                priceText = priceText.replace(",", "");
            }
            return Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            System.out.println("🚨 [가격 변환 오류] " + e.getMessage());
            return 0.0;
        }
    }
    

    // /**
    //  * ✅ 추가 이미지 크롤링
    //  */
    // private List<String> extractAdditionalImages(Page page) {
    //     List<String> images = new ArrayList<>();
    //     for (Locator imgLocator : page.locator("div.prod-image img").all()) {
    //         String imgSrc = imgLocator.getAttribute("src");
    //         if (imgSrc != null && !imgSrc.trim().isEmpty()) {
    //             images.add(imgSrc);
    //         }
    //     }
    //     return images;
    // }

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
