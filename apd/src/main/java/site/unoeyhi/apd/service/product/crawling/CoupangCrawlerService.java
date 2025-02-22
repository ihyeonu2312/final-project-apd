package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;

import java.util.List;
import java.util.Map;

@Service
public class CoupangCrawlerService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CoupangCrawlerService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * ✅ 카테고리별 상품 크롤링 (카테고리당 10개)
     */
    public void crawlProductsByCategory(Category category) {
        String categoryUrl = "https://www.coupang.com" + category.getUrl(); // ✅ 쿠팡 카테고리 URL 가져오기

        try (Playwright playwright = Playwright.create()) {
            // ✅ Playwright 설정 추가
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(false) // ✅ 브라우저 UI를 띄움 (차단 확인용)
                    .setArgs(List.of("--disable-blink-features=AutomationControlled")) // ✅ 자동화 탐지 방지
            );

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setExtraHTTPHeaders(Map.of(
                    "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
                    "Accept-Language", "ko-KR,ko;q=0.9",
                    "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
                ))
            );

            Page page = context.newPage();
            System.out.println("🚀 [크롤링 시작] " + category.getCategoryName() + " - " + categoryUrl);
            
            // ✅ 쿠팡 카테고리 페이지 이동 (타임아웃 증가)
            page.navigate(categoryUrl, new Page.NavigateOptions().setTimeout(90000)); 
            page.waitForLoadState(LoadState.NETWORKIDLE); // ✅ 네트워크 요청 끝날 때까지 대기

            System.out.println("✅ 페이지 로드 완료: " + page.title());

            // ✅ 상품 리스트 가져오기
        List<ElementHandle> productElements = page.querySelectorAll("li.baby-product.renew-badge");

        if (productElements.isEmpty()) {
            System.out.println("🚨 상품을 찾을 수 없습니다! CSS 선택자를 확인하세요.");
            return;
        }

        int count = 0;
        for (ElementHandle productElement : productElements) {
            if (count >= 10) break;
        
            // ✅ 상품명 크롤링 (null 체크 추가)
            ElementHandle nameElement = productElement.querySelector("a.baby-product-link");
            if (nameElement == null) {
                System.out.println("🚨 상품명 요소를 찾을 수 없습니다!");
                continue;
            }
            String name = nameElement.innerText();
        
            // ✅ 상세페이지 URL 크롤링 (null 체크 추가)
            String detailUrl = "https://www.coupang.com" + nameElement.getAttribute("href");
        
            // ✅ 상품 이미지 URL 크롤링 (null 체크 추가)
            ElementHandle imageElement = productElement.querySelector("img");
            String imageUrl = (imageElement != null) ? imageElement.getAttribute("src") : "";

            // ✅ 원가 선택자 (할인이 없는 경우)
            ElementHandle basePriceElement = productElement.querySelector("del.base-price");

            // ✅ 할인가 선택자 (할인이 적용된 경우)
            ElementHandle salePriceElement = productElement.querySelector("span.price");

            String priceText = "0";
            if (basePriceElement != null) {
                priceText = basePriceElement.innerText().replace(",", "").trim();
            } else if (salePriceElement != null) {
                priceText = salePriceElement.innerText().replace(",", "").trim();
            }

            // ✅ 숫자로 변환
            Double price = 0.0;
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                System.out.println("🚨 [가격 오류] 변환 실패: " + priceText);
            }

            // ✅ 가격이 0원이면 다음 상품으로 스킵
            if (price == 0.0) {
                System.out.println("⏩ [상품 스킵] 가격이 0원이므로 다음 상품으로 이동");
                continue;
            }

            // ✅ 디버깅 로그
            System.out.println("✅ [상품 가격] " + price);



        
            // ✅ 상품 저장
            Product product = Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(10)
                .category(category)
                .imageUrl(imageUrl)
                .thumbnailImageUrl(imageUrl)
                .detailUrl(detailUrl)
                .build();
        
            productRepository.save(product);
            System.out.println("✅ 상품 저장 완료: " + name);
            count++;
        }
        

                browser.close();
            } catch (Exception e) {
                System.out.println("🚨 크롤링 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
    }

    /**
     * ✅ 모든 카테고리에 대해 크롤링 실행
     */
    public void crawlAllCategories() {
        List<Category> categories = categoryRepository.findAll(); // ✅ DB에서 모든 카테고리 가져오기
        for (Category category : categories) {
            crawlProductsByCategory(category);
        }
    }
}
