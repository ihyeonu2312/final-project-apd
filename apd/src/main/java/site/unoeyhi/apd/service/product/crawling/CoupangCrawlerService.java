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
                .setArgs(List.of(
                    "--disable-http2",
                    "--disable-blink-features=AutomationControlled",
                    "--disable-gpu"
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
            context.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => false })");
    
            // 카테고리 페이지 열기
            Page categoryPage = context.newPage();
            categoryPage.navigate(categoryUrl, new Page.NavigateOptions()
                .setTimeout(60000)
                .setWaitUntil(WaitUntilState.NETWORKIDLE)
            );
            categoryPage.waitForTimeout(5000);

    
            // 상품 리스트 가져오기
            List<ElementHandle> productElements = categoryPage.querySelectorAll("li.baby-product.renew-badge");
            System.out.println("✅ 크롤링된 상품 개수: " + productElements.size());
    
            if (productElements.isEmpty()) {
                System.out.println("🚨 상품 없음 (선택자 확인 필요)");
                return;
            }
    
            int count = 0;
            // 상품 크롤링을 위한 페이지 열기
            for (ElementHandle productElement : productElements) {
                if (count >= 10) break;
    
                System.out.println("🔍 [디버깅] 현재 처리 중인 상품 인덱스: " + count);
    
                ElementHandle nameElement = productElement.querySelector("a.baby-product-link");
                if (nameElement == null) continue;
                String name = nameElement.innerText();
                String detailUrl = "https://www.coupang.com" + nameElement.getAttribute("href");
    
                // 가격 초기화
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
    
                // 이미지 URL 초기화
                String imageUrl = "";
                ElementHandle imageElement = productElement.querySelector("img");
                if (imageElement != null) {
                    imageUrl = imageElement.getAttribute("src");
                }
    
                // 추가 이미지 리스트 초기화
                List<String> additionalImages = new ArrayList<>();
                List<ElementHandle> imgElements = productElement.querySelectorAll("img");
                for (ElementHandle imgElement : imgElements) {
                    String imgSrc = imgElement.getAttribute("src");
                    if (imgSrc != null && !imgSrc.trim().isEmpty()) {
                        additionalImages.add(imgSrc);
                    }
                }
    
                // 상세 페이지 크롤링
                Page detailPage = context.newPage(); // 상품마다 새로운 상세 페이지 열기
                try {
                   // 안정적인 페이지 로딩을 위해 navigate() 사용
                    detailPage.navigate(detailUrl, new Page.NavigateOptions()
                        .setTimeout(90000)
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    );
                    // ✅ 페이지가 완전히 로딩될 때까지 기다림
                    detailPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    detailPage.waitForTimeout(5000);
                    System.out.println("📌 [디버깅] JavaScript URL 이동 후: " + detailPage.url());

                    // ✅ about:blank 상태인지 확인 후 재시도
                    int retryCount = 0;
                    while (detailPage.url().equals("about:blank") && retryCount < 3) {
                        System.out.println("🚨 [경고] 페이지가 about:blank 상태입니다. 새로고침 시도... (" + (retryCount + 1) + "/3)");
                        detailPage.reload();
                        detailPage.waitForTimeout(5000);
                        retryCount++;
                    }

                    if (detailPage.url().equals("about:blank")) {
                        System.out.println("🚨 [실패] 페이지가 여전히 about:blank 상태입니다. 상품 크롤링 스킵.");
                        detailPage.close();
                        continue;  // 다음 상품으로 이동
                    }

                    // 옵션 크롤링
                    List<OptionDto> optionList = new ArrayList<>();
                    ElementHandle optionWrapper = detailPage.querySelector("div#optionWrapper");
                    if (optionWrapper != null) {
                        ElementHandle optionButton = detailPage.querySelector("div#optionWrapper .single-attribute__textLabel");
                        if (optionButton != null) {
                            optionButton.click();
                            detailPage.waitForTimeout(1000);
                        }
    
                        detailPage.evaluate("document.querySelectorAll('div#optionWrapper ul.prod-option__item').forEach(e => e.style.display = 'block');");
                        detailPage.waitForSelector("div#optionWrapper ul.prod-option__item li", 
                            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE)
                        );
    
                        List<ElementHandle> optionElements = detailPage.querySelectorAll("div#optionWrapper ul.prod-option__item li");
                        for (ElementHandle optionElement : optionElements) {
                            String optionId = optionElement.getAttribute("data-attribute-id");
                            String optionValue = optionElement.innerText().trim();
                            if (optionId != null && !optionId.isEmpty() && optionValue != null && !optionValue.isEmpty()) {
                                optionList.add(new OptionDto("DEFAULT", optionValue));
                                System.out.println("🔹 [옵션 발견] 옵션 값: " + optionValue);
                            }
                        }
                    }
    
                    // 상품 데이터 생성 및 저장
                    ProductDto productDto = ProductDto.builder()
                        .name(name)
                        .price(price)
                        .stockQuantity(10)
                        .categoryId(category.getCategoryId())
                        .imageUrl(imageUrl)
                        .thumbnailImageUrl(imageUrl)
                        .detailUrl(detailUrl)
                        .additionalImages(additionalImages)
                        .options(optionList)
                        .build();
    
                    saveProductData(productDto);
                    System.out.println("✅ 저장 요청 완료: " + name);
    
                } catch (Exception e) {
                    System.out.println("🚨 [옵션 크롤링 오류] " + e.getMessage());
                } finally {
                    detailPage.waitForTimeout(1000);  // 1초 대기
                    detailPage.close();  // 상세 페이지 크롤링이 끝난 후 페이지 닫기
                }
    
                count++;
            }
    
            // 카테고리 페이지 크롤링이 끝난 후 카테고리 페이지 닫기
            categoryPage.close();
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
