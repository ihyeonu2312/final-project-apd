package site.unoeyhi.apd.service.product.crawling;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import site.unoeyhi.apd.dto.product.OptionDto;
import site.unoeyhi.apd.dto.product.ProductDto;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;
import site.unoeyhi.apd.service.product.ProductService;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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
        System.out.println("🚀 [테스트] 모든 카테고리에서 상품 크롤링 시작!"); // ✅ 여기까지 실행되는지 확인
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            System.out.println("🚨 [크롤링 중단] 크롤링할 카테고리가 없습니다!");
            return;
        }
        for (Category category : categories) {
            System.out.println("📌 [카테고리] ID: " + category.getCategoryId() + " | Name: " + category.getCategoryName());
            System.out.println("📌 [카테고리] 크롤링 시작 - " + category.getCategoryName()); // ✅ 카테고리 출력
            crawlProductsByCategory(category);
        }
        System.out.println("✅ [크롤링 완료]");
    }

    public void crawlProductsByCategory(Category category) {
        String categoryUrl = "https://www.coupang.com" + category.getUrl();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(List.of("--disable-http2", "--disable-blink-features=AutomationControlled"))
            );
    
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setJavaScriptEnabled(false)
            );
    
            Page page = context.newPage();
            page.navigate(categoryUrl);
            page.waitForLoadState(LoadState.LOAD);
            page.waitForTimeout(3000); // ✅ 추가 대기
    
            // ✅ 상품 리스트 HTML 일부 확인 (올바른 선택자로 변경)
            String productListHtml = page.innerHTML("ul#productList"); // ✅ 올바른 상품 리스트 컨테이너
            System.out.println("📌 [디버깅] 상품 리스트 HTML:\n" + productListHtml);
    
            // ✅ 상품이 로드될 때까지 대기 (이전보다 안정적인 크롤링 가능)
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
    
                System.out.println("📌 [디버깅] 상품 데이터 변환 완료 - Name: " + name + ", Price: " + price + ", Image: " + imageUrl);
    
                ProductDto productDto = ProductDto.builder()
                    .name(name)
                    .price(price)
                    .stockQuantity(10)
                    .categoryId(category.getCategoryId())
                    .imageUrl(imageUrl)
                    .thumbnailImageUrl(imageUrl)
                    .detailUrl(detailUrl)
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
            System.out.println("📌 [saveProductData] 상품 데이터 저장 요청 - " + productDto.getName());
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
    