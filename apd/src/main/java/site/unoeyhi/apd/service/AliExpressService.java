package site.unoeyhi.apd.service;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.model.CategoryModel;
import site.unoeyhi.apd.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AliExpressService {

    private final CategoryRepository categoryRepository;

    public AliExpressService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ✅ DB에서 모든 카테고리 조회 (Entity → Model 변환 후 반환)
    public List<CategoryModel> getAllCategory() {
        return categoryRepository.findAll().stream()
                .map(entity -> new CategoryModel(entity.getCategoryId(), entity.getName(), entity.getUrl()))
                .collect(Collectors.toList());
    }

    // ✅ 크롤링 실행 후 DB에 저장
    @Transactional
    public List<CategoryModel> scrapAndSaveCategories() {
        List<CategoryModel> categoryList = new ArrayList<>(); // 크롤링 데이터 저장 리스트

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setArgs(List.of("--disable-blink-features=AutomationControlled")));

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                    .setViewportSize(1366, 768));

            Page page = context.newPage();

            System.out.println("🔵 페이지 이동 시작");
            page.navigate("https://www.aliexpress.com/");
            page.waitForTimeout(5000);
            System.out.println("✅ 페이지 이동 완료");

            // ✅ 카테고리 메뉴 로드 대기
            System.out.println("🔵 카테고리 메뉴 대기 중...");
            try {
                page.waitForSelector("ul.Categoey--categoryList--2QES_k6 > a",
                        new Page.WaitForSelectorOptions().setTimeout(10000));
                System.out.println("✅ 카테고리 메뉴 확인 완료!");
            } catch (Exception e) {
                System.out.println("⚠️ 카테고리 메뉴를 찾을 수 없음!");
                return categoryList; // 크롤링 실패 시 빈 리스트 반환
            }

            // ✅ 크롤링 실행
            System.out.println("🔵 카테고리 스크랩 시작");
            try {
                Locator categories = page.locator("ul.Categoey--categoryList--2QES_k6 > a");

                if (categories.count() > 0) {
                    categories.all().forEach(category -> {
                        String categoryName = category.locator("div.Categoey--categoryItemTitle--2uJUqT2").textContent().trim();
                        String categoryUrl = category.getAttribute("href");

                        // **중복 데이터 확인 후 저장**
                        Optional<Category> existingCategory = categoryRepository.findByName(categoryName);
                        if (existingCategory.isPresent()) {
                            System.out.println("⚠️ 중복 카테고리: " + categoryName);
                        } else {
                            // **DB 저장**
                            Category newCategory = Category.builder()
                                    .name(categoryName)
                                    .url(categoryUrl)
                                    .build();
                            Category savedCategory = categoryRepository.save(newCategory);

                            // **DTO 변환 후 리스트 추가**
                            categoryList.add(new CategoryModel(savedCategory.getCategoryId(), savedCategory.getName(), savedCategory.getUrl()));
                        }
                    });
                    System.out.println("✅ 카테고리 저장 완료");
                } else {
                    System.out.println("⚠️ 카테고리 목록을 찾을 수 없음");
                }
            } catch (Exception e) {
                System.out.println("⚠️ 스크랩 중 오류 발생: " + e.getMessage());
            }

            System.out.println("📌 최종 저장 결과: " + categoryList);
            browser.close();
        }

        return categoryList; // 최종 크롤링 및 저장된 데이터 반환
    }
}
