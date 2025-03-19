package site.unoeyhi.apd.service.product.crawling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

        // ✅ 랜덤 딜레이 (500ms ~ 3초)
        int randomDelay = ThreadLocalRandom.current().nextInt(500, 3000);
        detailPage.waitForTimeout(randomDelay);

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
        // ✅ 상품 간 랜덤 대기 시간 추가 (2초 ~ 5초)
        int randomPageDelay = ThreadLocalRandom.current().nextInt(2000, 5000);
        try {
            Thread.sleep(randomPageDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
}

    /** ✅ 상세 이미지 추출 */
    public List<String> extractDetailImages(Page detailPage) {
        List<String> images = new ArrayList<>();
    
        // ✅ 상세 이미지 요소 로딩 대기 (변경됨)
        detailPage.waitForSelector("div.product-detail-content-inside img", new Page.WaitForSelectorOptions().setTimeout(10000));
    
        // ✅ `product-detail-content` 내 모든 이미지 가져오기
        List<Locator> imgLocators = detailPage.locator("div.product-detail-content-inside img").all();
    
        for (Locator imgLocator : imgLocators) {
            imgLocator.scrollIntoViewIfNeeded(); // ✅ 이미지가 보이도록 스크롤
    
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
