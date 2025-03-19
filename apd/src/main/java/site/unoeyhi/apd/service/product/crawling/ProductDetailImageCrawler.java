package site.unoeyhi.apd.service.product.crawling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

import site.unoeyhi.apd.service.product.ProductDetailImageService;

@Service
public class ProductDetailImageCrawler {

    private final ProductDetailImageService productDetailImageService;

    @Autowired
    public ProductDetailImageCrawler(ProductDetailImageService productDetailImageService) {
        this.productDetailImageService = productDetailImageService;
    }

    /**
     * ✅ 상세 페이지에서 이미지 크롤링 후 DB에 저장하는 메서드
     */
    public void crawlAndSaveDetailImages(BrowserContext context, Long productId, String detailUrl) {
        System.out.println("🚀 [ProductDetailImageCrawler] 상세 이미지 크롤링 시작: " + detailUrl);

        Page detailPage = context.newPage();

        try {
            // ✅ 페이지 이동 및 네트워크 대기
            detailPage.navigate(detailUrl, new Page.NavigateOptions()
                .setTimeout(60000)
                .setWaitUntil(WaitUntilState.NETWORKIDLE)); // ✅ 네트워크 요청이 끝날 때까지 기다림

            // ✅ 이미지가 로딩될 때까지 최대 5초 대기
            detailPage.waitForSelector("#productDetail img", new Page.WaitForSelectorOptions().setTimeout(5000));

            // ✅ 상세 이미지 URL 추출
            List<String> imageUrls = extractDetailImages(detailPage);

            if (imageUrls.isEmpty()) {
                System.out.println("⚠️ [경고] 상세 이미지가 없음! " + detailUrl);
                return;
            }

            System.out.println("📸 [크롤링 성공] 상세 이미지 개수: " + imageUrls.size());

            // ✅ 상세 이미지 DB 저장
            productDetailImageService.saveDetailImages(productId, imageUrls);

        } catch (Exception e) {
            System.out.println("🚨 [오류 발생] 크롤링 실패: " + e.getMessage());
        } finally {
            detailPage.close();
        }
    }

    /**
     * ✅ 상세 이미지 추출 메서드
     */
    public List<String> extractDetailImages(Page detailPage) {
        Set<String> imageSet = new HashSet<>(); // ✅ 중복 방지
        List<String> images = new ArrayList<>();

        // ✅ 상세 이미지 `#productDetail img`에서 가져오기
        List<Locator> imgLocators = detailPage.locator("#productDetail img").all();

        for (Locator imgLocator : imgLocators) {
            String imgSrc = imgLocator.getAttribute("src");
            if (imgSrc != null && !imgSrc.trim().isEmpty() && imageSet.add(imgSrc)) {
                images.add(imgSrc);
                System.out.println("✅ [상세 이미지 발견] " + imgSrc);
            }
        }

        System.out.println("📸 [상세 이미지 크롤링 완료] 총 " + images.size() + "개 발견");
        return images;
    }
}
