package site.unoeyhi.apd.service.product.crawling;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CoupangImageDownloader {
    private static final String BASE_DATA_PATH = "C:\\coupang\\data\\";
    private static final String MAX_IMAGE_SIZE = "1000x1000ex";

    public static void main(String[] args) {
        File[] productFolders = new File(BASE_DATA_PATH).listFiles(File::isDirectory);
        if (productFolders == null) return;

        for (File folder : productFolders) {
            File htmlFile = new File(folder, "page.html");
            if (!htmlFile.exists()) continue;

            try {
                // ✅ HTML 파일 로드
                Document document = Jsoup.parse(htmlFile, "UTF-8");

                // ✅ 이미지 URL 추출
                List<String> imageUrls = new ArrayList<>();
                Elements imageElements = document.select(".prod-image__items .prod-image__item img[src]");

                for (Element img : imageElements) {
                    String imgUrl = img.attr("src");
                    imgUrl = getMaxSizeImageUrl(imgUrl);
                    imageUrls.add(imgUrl);
                }

                // ✅ 이미지 다운로드
                saveProductImages(imageUrls, folder.getAbsolutePath());

            } catch (IOException e) {
                System.out.println("⚠ HTML 파싱 실패: " + htmlFile.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    private static String getMaxSizeImageUrl(String imgUrl) {
        if (imgUrl.contains("thumbnail") && imgUrl.matches(".*?/\\d+x\\d+ex/.*")) {
            return imgUrl.replaceAll("/\\d+x\\d+ex/", "/" + MAX_IMAGE_SIZE + "/");
        }
        return imgUrl;
    }

    private static void saveProductImages(List<String> imageUrls, String productDataPath) {
        try {
            Files.createDirectories(Paths.get(productDataPath));

            int imageCount = 1;
            for (String imgUrl : imageUrls) {
                if (!imgUrl.startsWith("http")) continue;

                String fileExtension = imgUrl.substring(imgUrl.lastIndexOf("."));
                String saveFileName = productDataPath + "\\" + imageCount + fileExtension;

                downloadImage(imgUrl, saveFileName);
                imageCount++;
            }
            System.out.println("✅ 상품 이미지 저장 완료: " + productDataPath);

        } catch (Exception e) {
            System.out.println("⚠ 이미지 저장 실패: " + productDataPath);
            e.printStackTrace();
        }
    }

    private static void downloadImage(String imgUrl, String savePath) {
        try (InputStream in = new URL(imgUrl).openStream();
             FileOutputStream out = new FileOutputStream(savePath)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("📷 저장 완료: " + savePath);
        } catch (Exception e) {
            System.out.println("⚠ 이미지 다운로드 실패: " + imgUrl);
            e.printStackTrace();
        }
    }
}
