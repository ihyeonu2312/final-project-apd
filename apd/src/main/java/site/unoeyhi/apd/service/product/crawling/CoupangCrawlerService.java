package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.dto.product.OptionDto;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductImage;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductImageRepository;
import site.unoeyhi.apd.service.product.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CoupangCrawlerService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final ProductImageRepository productImageRepository;

    public CoupangCrawlerService(CategoryRepository categoryRepository, ProductService productService,ProductImageRepository productImageRepository) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
        this.productImageRepository = productImageRepository;
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
                .setArgs(List.of("--disable-http2", "--disable-blink-features=AutomationControlled", "--disable-gpu"))
            );

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setJavaScriptEnabled(true)
                .setExtraHTTPHeaders(Map.of(
                    "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
                    "Accept-Language", "ko-KR,ko;q=0.9",
                    "Referer", "https://www.coupang.com/",
                    "X-Forwarded-For", "220.95.91.1"
                ))
            );
            context.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => false })");

            Page currentPage = context.newPage();
            currentPage.navigate(categoryUrl, new Page.NavigateOptions().setTimeout(120000).setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            currentPage.waitForTimeout(5000);

            List<ElementHandle> productElements = currentPage.querySelectorAll("li.baby-product.renew-badge");
            if (productElements.isEmpty()) {
                System.out.println("🚨 상품 없음 (선택자 확인 필요)");
                return;
            }

            int count = 0;
            for (ElementHandle productElement : productElements) {
                if (count >= 10) break;

                // ✅ 상품명 크롤링 (목록 페이지에서 가져옴, 위치 그대로 유지)
                ElementHandle nameElement = productElement.querySelector("div.name");
                String name = (nameElement != null) ? nameElement.innerText().trim() : "알 수 없음";
                System.out.println("🏷️ [디버깅] 상품명: " + name);

                // ✅ 상품 상세 페이지 URL 크롤링 (위치 그대로 유지)
                ElementHandle linkElement = productElement.querySelector("a.baby-product-link");
                String detailUrl = (linkElement != null) ? "https://www.coupang.com" + linkElement.getAttribute("href") : "";
                System.out.println("🔍 [디버깅] 상품 상세 URL: " + detailUrl);


                // ✅ 상품 상세 페이지 크롤링 로직 개선
                try {
                    Page detailPage = context.newPage();
                    boolean success = false;
                    int retryCount = 0;

                    while (!success && retryCount < 3) {
                        try {
                            detailPage.navigate(detailUrl, new Page.NavigateOptions()
                                .setTimeout(60000)
                                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                            );

                            detailPage.waitForTimeout(5000);
                            if (!detailPage.url().equals("about:blank")) {
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

                    // ✅ 가격 크롤링
                    Locator priceLocator = detailPage.locator("strong.price-value");
                    String priceText = priceLocator.isVisible() ? priceLocator.innerText().replace(",", "").trim() : "0";
                    Double price = priceText.isEmpty() ? 0.0 : Double.parseDouble(priceText);
                    System.out.println("💰 [디버깅] 상품 가격: " + price);

                    // ✅ 대표 이미지 크롤링
                    Locator imageLocator = detailPage.locator("div.prod-image img").first();
                    String imageUrl = imageLocator.isVisible() ? imageLocator.getAttribute("src") : "";
                    System.out.println("🖼️ [디버깅] 대표 이미지 URL: " + imageUrl);

                    // ✅ 추가 이미지 크롤링
                    List<String> additionalImages = new ArrayList<>();
                    List<Locator> imageLocators = detailPage.locator("div.prod-image img").all();
                    for (Locator imgLocator : imageLocators) {
                        if (imgLocator.isVisible()) {
                            String imgSrc = imgLocator.getAttribute("src");
                            if (imgSrc != null && !imgSrc.trim().isEmpty() && !imgSrc.equals(imageUrl)) {
                                additionalImages.add(imgSrc);
                            }
                        }
                    }
                    System.out.println("📸 [디버깅] 추가 이미지 개수: " + additionalImages.size());

                    // ✅ 옵션 크롤링 (옵션이 없는 경우 대비)
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
                    
                    // ✅ 옵션이 없는 경우 처리
                    if (optionList.isEmpty()) {
                        System.out.println("⚠️ [디버깅] 옵션이 없는 상품입니다.");
                    } else {
                        System.out.println("🎯 [디버깅] 옵션 개수: " + optionList.size());
                    }

                    // ✅ 상품 데이터 저장 (builder 사용)
                    ProductDto productDto = ProductDto.builder()
                        .name(name)
                        .price(price)
                        .stockQuantity(10)
                        .categoryId(category.getCategoryId())
                        .imageUrl(imageUrl)
                        .thumbnailImageUrl(imageUrl)
                        .detailUrl(detailUrl)
                        .options(optionList)
                        .additionalImages(additionalImages) // ✅ 추가 이미지 포함
                        .build();

                    saveProductData(productDto);

                } catch (Exception e) {
                    System.out.println("🚨 [크롤링 오류] " + e.getMessage());
                }
                count++;
            }
            browser.close();
        }
    }

        @Transactional
    public void saveProductData(ProductDto productDto) {
        try {
            // ✅ 상품 저장
            Product savedProduct = productService.saveProduct(productDto);
            System.out.println("✅ [saveProduct] 상품 저장 완료: " + savedProduct.getName());

            // ✅ 추가 이미지 저장 (비어있지 않은 경우만)
            if (productDto.getAdditionalImages() != null && !productDto.getAdditionalImages().isEmpty()) {
                for (String imageUrl : productDto.getAdditionalImages()) {
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) { // ✅ 빈 값 필터링
                        ProductImage productImage = ProductImage.builder()
                                .product(savedProduct) // ✅ 저장된 상품과 연결
                                .imageUrl(imageUrl)
                                .build();
                        productImageRepository.save(productImage); // ✅ 추가 이미지 저장
                        System.out.println("🖼️ [saveProduct] 추가 이미지 저장 완료: " + imageUrl);
                    }
                }
            } else {
                System.out.println("⚠️ [saveProduct] 추가 이미지가 없습니다!");
            }
        } catch (Exception e) {
            System.out.println("🚨 [saveProduct] 상품 저장 실패: " + e.getMessage());
        }
    }
}

