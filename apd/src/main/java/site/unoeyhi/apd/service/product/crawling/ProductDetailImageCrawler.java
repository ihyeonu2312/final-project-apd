package site.unoeyhi.apd.service.product.crawling;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

import site.unoeyhi.apd.service.product.ProductDetailImageService;

@Service
public class ProductDetailImageCrawler {

    @Autowired
    private ProductDetailImageService productDetailImageService;

    /** ✅ 상세 이미지 크롤링 & DB 저장 */
    public void crawlAndSaveDetailImages(BrowserContext context, Long productId, String detailUrl) {
        System.out.println("🚀 [ProductDetailImageCrawler] 상세 이미지 크롤링 시작: " + detailUrl);

        Page detailPage = context.newPage();
        detailPage.navigate(detailUrl, new Page.NavigateOptions()
                .setTimeout(60000)
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // ✅ 상세 이미지 URL 추출
        List<String> imageUrls = extractDetailImages(detailPage);

        if (imageUrls.isEmpty()) {
            System.out.println("⚠️ [경고] 상세 이미지 없음! " + detailUrl);
            return;
        }

        System.out.println("📸 [크롤링 성공] 상세 이미지 개수: " + imageUrls.size());

        // ✅ 상세 이미지 DB 저장
        productDetailImageService.saveDetailImages(productId, imageUrls);

        detailPage.close();
    }

    /** ✅ 상세 이미지 추출 */
    public List<String> extractDetailImages(Page detailPage) {
        List<String> images = new ArrayList<>();

        // ✅ 상품 상세 페이지에서 이미지 찾기
        List<Locator> imgLocators = detailPage.locator("div#productDetail img").all();

        for (Locator imgLocator : imgLocators) {
            String imgSrc = imgLocator.getAttribute("src");
            if (imgSrc != null && !imgSrc.trim().isEmpty()) {
                images.add(imgSrc);
                System.out.println("✅ [상세 이미지 발견] " + imgSrc);
            }
        }

        System.out.println("📸 [상세 이미지 크롤링 완료] 총 " + images.size() + "개 발견");
        return images;
    }
}
