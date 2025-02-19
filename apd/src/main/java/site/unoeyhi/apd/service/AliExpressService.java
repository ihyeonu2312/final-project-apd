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
        List<CategoryModel> categoryList = new ArrayList<>();

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

            // ✅ 카테고리 메뉴 활성화 (호버 후 3초 대기)
            try {
                System.out.println("🔵 카테고리 메뉴 활성화...");
                page.locator("div[data-spm='allcategoriespc']").hover();
                page.waitForTimeout(3000);
                System.out.println("✅ 카테고리 메뉴 활성화 완료!");
            } catch (Exception e) {
                System.out.println("⚠️ 카테고리 메뉴 활성화 실패: " + e.getMessage());
            }

            // ✅ 카테고리 목록 대기
            System.out.println("🔵 카테고리 목록 대기...");
            try {
                page.waitForSelector("ul.Categoey--categoryList--2QES_k6", new Page.WaitForSelectorOptions().setTimeout(10000));
                System.out.println("✅ 카테고리 목록 로드 완료!");
            } catch (Exception e) {
                System.out.println("⚠️ 카테고리 목록을 찾을 수 없음! " + e.getMessage());
                return categoryList;
            }

            // ✅ 카테고리 스크랩 시작
            System.out.println("🔵 카테고리 스크랩 시작");
            try {
                Locator categories = page.locator("ul.Categoey--categoryList--2QES_k6 > a");
                int categoryCount = categories.count();
                System.out.println("📌 카테고리 개수: " + categoryCount);

                if (categoryCount > 0) {
                    for (int i = 0; i < categoryCount; i++) {
                        String categoryName = categories.nth(i).locator("div[class*='categoryItemTitle']").textContent().trim();
                        String categoryUrl = categories.nth(i).getAttribute("href");

                        Optional<Category> existingCategory = categoryRepository.findByCategoryName(categoryName);
                        if (existingCategory.isPresent()) {
                            System.out.println("⚠️ 중복 카테고리: " + categoryName);
                        } else {
                            Category newCategory = Category.builder()
                                    .name(categoryName)
                                    .url(categoryUrl)
                                    .build();
                            Category savedCategory = categoryRepository.save(newCategory);
                            categoryList.add(new CategoryModel(savedCategory.getCategoryId(), savedCategory.getName(), savedCategory.getUrl()));
                        }
                    }
                    System.out.println("✅ 카테고리 저장 완료");
                } else {
                    System.out.println("⚠️ 카테고리 목록을 찾을 수 없음");
                }
            } catch (Exception e) {
                System.out.println("⚠️ 크롤링 중 오류 발생: " + e.getMessage());
            }

            System.out.println("📌 최종 저장 결과: " + categoryList);
            browser.close();
        }

        return categoryList;
    }
}
