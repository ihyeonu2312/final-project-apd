package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import site.unoeyhi.apd.dto.product.OptionDto;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;
import site.unoeyhi.apd.service.product.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CoupangCrawlerService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public CoupangCrawlerService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productService = productService;
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
            System.out.println("📌 [카테고리] 크롤링 시작 - " + category.getCategoryName());
            crawlProductsByCategory(category);
        }
        System.out.println("✅ [크롤링 완료]");
    }

    public void crawlProductsByCategory(Category category) {
        String categoryUrl = "https://www.coupang.com" + category.getUrl();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(List.of
                ("--disable-http2",
                 "--disable-blink-features=AutomationControlled", // 자동화 탐지 방지
                 "--disable-gpu" // GPU 가속 비활성화 (안정성 증가)
                 ))
            );

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setJavaScriptEnabled(true)
                .setExtraHTTPHeaders(Map.of(
                    "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
                    "Accept-Language", "ko-KR,ko;q=0.9",
                    "Referer", "https://www.coupang.com/"
                ))
            );

            Page page = context.newPage();
            page.navigate(categoryUrl);
            page.waitForLoadState(LoadState.LOAD);
            page.waitForTimeout(3000);

            // ✅ 상품 리스트 확인
            String productListHtml = page.innerHTML("ul#productList");
            System.out.println("📌 [디버깅] 상품 리스트 HTML:\n" + productListHtml);

            // ✅ 상품이 로드될 때까지 대기
            page.waitForSelector("li.baby-product.renew-badge", 
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));

            // ✅ 상품 리스트 가져오기
            List<ElementHandle> productElements = page.querySelectorAll("li.baby-product.renew-badge");
            System.out.println("✅ 크롤링된 상품 개수: " + productElements.size());

            if (productElements.isEmpty()) {
                System.out.println("🚨 상품 없음 (선택자 확인 필요)");
                System.out.println("📌 현재 페이지 HTML:\n" + page.content());
                return;
            }

            int count = 0;
            for (ElementHandle productElement : productElements) {
                if (count >= 10) break;

                ElementHandle nameElement = productElement.querySelector("a.baby-product-link");
                if (nameElement == null) continue;
                String name = nameElement.innerText();
                String detailUrl = "https://www.coupang.com" + nameElement.getAttribute("href");

                ElementHandle imageElement = productElement.querySelector("img");
                String imageUrl = (imageElement != null) ? imageElement.getAttribute("src") : "";

                String priceText = "0";
                ElementHandle priceElement = productElement.querySelector("strong.price-value");

                if (priceElement != null) {
                    priceText = priceElement.innerText().replace(",", "").trim();
                }

                Double price = 0.0;
                try {
                    price = Double.parseDouble(priceText);
                } catch (NumberFormatException e) {
                    System.out.println("🚨 [가격 오류] " + priceText);
                }

                if (price == 0.0) continue;

                // ✅ 추가 이미지 가져오기
                List<String> additionalImages = new ArrayList<>();
                List<ElementHandle> imageElements = productElement.querySelectorAll("img");
                for (ElementHandle imgElement : imageElements) {
                    String imgUrl = imgElement.getAttribute("src");
                    if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                        additionalImages.add(imgUrl);
                    }
                }

                // ✅ 옵션 크롤링 (상세 페이지에서 진행)
                List<OptionDto> optionList = new ArrayList<>();
                Page detailPage = context.newPage();  // ✅ 새로운 페이지 열기

                try {
                    detailPage.navigate(detailUrl ,new Page.NavigateOptions()
                        .setTimeout(60000) // ✅ 타임아웃을 60초로 증가
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED) // ✅ LoadState 대신 DOMContentLoaded 사용
                    );
                    detailPage.waitForTimeout(5000); // ✅ 페이지 로드 대기

                    // ✅ 옵션 버튼이 있는지 확인하고 클릭하여 펼치기
                    ElementHandle optionButton = detailPage.querySelector("div#optionWrapper .single-attribute__textLabel");
                    if (optionButton != null) {
                        optionButton.click();
                        detailPage.waitForTimeout(1000); // ✅ 클릭 후 1초 대기
                    }

                    // ✅ JavaScript 실행: 숨겨진 옵션을 보이게 함
                    detailPage.evaluate("document.querySelectorAll('div#optionWrapper ul.prod-option__item').forEach(e => e.style.display = 'block');");

                    // ✅ 옵션이 로드될 때까지 대기
                    detailPage.waitForSelector("div#optionWrapper ul.prod-option__item li", 
                        new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));

                    // ✅ 모든 옵션 요소 가져오기
                    List<ElementHandle> optionElements = detailPage.querySelectorAll("div#optionWrapper ul.prod-option__item li");
                    System.out.println("🛠️ [옵션 크롤링] 옵션 개수: " + optionElements.size());

                    for (ElementHandle optionElement : optionElements) {
                        String optionId = optionElement.getAttribute("data-attribute-id");
                        String optionValue = optionElement.innerText().trim();

                        if (optionId != null && !optionId.isEmpty() && optionValue != null && !optionValue.isEmpty()) {
                            optionList.add(new OptionDto("DEFAULT", optionValue));
                            System.out.println("🔹 [옵션 발견] 옵션 값: " + optionValue);
                        }
                    }

                } catch (Exception e) {
                    System.out.println("🚨 [옵션 크롤링 오류] " + e.getMessage());
                } finally {
                    detailPage.close(); // ✅ 상세 페이지 닫기
                }


                // ✅ 상품 데이터 생성
                ProductDto productDto = ProductDto.builder()
                    .name(name)
                    .price(price)
                    .stockQuantity(10)
                    .categoryId(category.getCategoryId())
                    .imageUrl(imageUrl)
                    .thumbnailImageUrl(imageUrl)
                    .detailUrl(detailUrl)
                    .additionalImages(additionalImages) // ✅ 추가 이미지 리스트 저장
                    .options(optionList) // ✅ 옵션 리스트 저장
                    .build();

                saveProductData(productDto);
                System.out.println("✅ 저장 요청 완료: " + name);
                count++;
            }

            browser.close();
        } catch (Exception e) {
            System.out.println("🚨 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void saveProductData(ProductDto productDto) {
        System.out.println("📌 [saveProductData] 상품 데이터 저장 요청 - " + (productDto != null ? productDto.getName() : "NULL PRODUCT DTO"));
        try {
            Product savedProduct = productService.saveProduct(productDto);
            productRepository.flush();  // ✅ 강제 flush 실행
            System.out.println("✅ [saveProductData] 저장된 상품 ID: " + savedProduct.getProductId());

            // ✅ DB 저장 후 개수 확인
            long productCount = productRepository.count();
            System.out.println("📌 [DB 저장 후] 현재 DB 상품 개수: " + productCount);
        } catch (Exception e) {
            System.out.println("🚨 [saveProductData] 상품 저장 실패: " + e.getMessage());
        }
    }
}
