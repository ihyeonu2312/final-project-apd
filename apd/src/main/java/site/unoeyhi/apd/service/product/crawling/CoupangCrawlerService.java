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
                    "Referer", categoryUrl
                ))
            );
            context.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => false })");

            Page currentPage = context.newPage();
            currentPage.navigate(categoryUrl, new Page.NavigateOptions()
                .setTimeout(60000)
                .setWaitUntil(WaitUntilState.NETWORKIDLE));
            currentPage.waitForTimeout(5000);

            List<ElementHandle> productElements = currentPage.querySelectorAll("li.baby-product.renew-badge");
            if (productElements.isEmpty()) {
                System.out.println("🚨 상품 없음 (선택자 확인 필요)");
                return;
            }

            int count = 0;
            for (ElementHandle productElement : productElements) {
                if (count >= 10) break;

                ElementHandle nameElement = productElement.querySelector("a.baby-product-link");
                if (nameElement == null) continue;
                String name = nameElement.innerText();
                String detailUrl = "https://www.coupang.com" + nameElement.getAttribute("href");

                try {
                    System.out.println("🔍 [디버깅] 상품 이동: " + detailUrl);

                    // ✅ 새로운 상세 페이지 열기 (기존 페이지에서 이동하지 않음)
                    Page detailPage = context.newPage();
                    detailPage.navigate(detailUrl, new Page.NavigateOptions()
                        .setTimeout(90000)
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    detailPage.waitForLoadState(LoadState.LOAD);
                    detailPage.waitForTimeout(5000);

                    // ✅ about:blank 상태인지 확인 후 재시도
                    int retryCount = 0;
                    while (detailPage.url().equals("about:blank") && retryCount < 3) {
                        System.out.println("🚨 [경고] 페이지가 about:blank 상태입니다. 새로고침 시도... (" + (retryCount + 1) + "/3)");
                        detailPage.reload();
                        retryCount++;
                        detailPage.waitForTimeout(5000);
                    }

                    if (detailPage.url().equals("about:blank")) {
                        System.out.println("🚨 [실패] 페이지가 여전히 about:blank 상태입니다. 상품 크롤링 스킵.");
                        detailPage.close();
                        continue;
                    }

                    // ✅ 가격 가져오기
                    ElementHandle priceElement = detailPage.querySelector("strong.price-value");
                    String priceText = (priceElement != null) ? priceElement.innerText().replace(",", "").trim() : "0";
                    Double price = priceText.isEmpty() ? 0.0 : Double.parseDouble(priceText);

                    // ✅ 이미지 가져오기
                    ElementHandle imageElement = detailPage.querySelector("img");
                    String imageUrl = (imageElement != null) ? imageElement.getAttribute("src") : "";

                    List<String> additionalImages = new ArrayList<>();
                    List<ElementHandle> imgElements = detailPage.querySelectorAll("img");
                    for (ElementHandle imgElement : imgElements) {
                        String imgSrc = imgElement.getAttribute("src");
                        if (imgSrc != null && !imgSrc.trim().isEmpty()) {
                            additionalImages.add(imgSrc);
                        }
                    }

                    // ✅ 옵션 크롤링
                    List<OptionDto> optionList = new ArrayList<>();
                    ElementHandle optionWrapper = detailPage.querySelector("div#optionWrapper");
                    if (optionWrapper != null) {
                        List<ElementHandle> optionElements = detailPage.querySelectorAll("div#optionWrapper ul.prod-option__item li");
                        for (ElementHandle optionElement : optionElements) {
                            String optionValue = optionElement.innerText().trim();
                            if (!optionValue.isEmpty()) {
                                optionList.add(new OptionDto("DEFAULT", optionValue));
                                System.out.println("🔹 [옵션 발견] 옵션 값: " + optionValue);
                            }
                        }
                    }

                    // ✅ 상품 데이터 저장
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
                    
                    detailPage.close();  // ✅ 상세 페이지 닫기

                } catch (Exception e) {
                    System.out.println("🚨 [크롤링 오류] " + e.getMessage());
                }
                count++;
            }

            browser.close();
        } catch (Exception e) {
            System.out.println("🚨 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void saveProductData(ProductDto productDto) {
        productService.saveProduct(productDto);
    }
}
