package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.net.URL;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductDetailImage;
import site.unoeyhi.apd.repository.product.ProductDetailImageRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryUploadService {

    private final ProductRepository productRepository;
    private final ProductDetailImageRepository detailImageRepository;
    private final RestTemplate restTemplate;


    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Value("${cloudinary.upload_preset}")
    private String uploadPreset;

    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/%s/image/upload";
   @Transactional
public void uploadAndUpdateImages() {
    ExecutorService executor = Executors.newFixedThreadPool(5); // 병렬 5개
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    // 상품 대표 이미지, 썸네일 업로드
    List<Product> products = productRepository.findAll();
    for (Product product : products) {
        futures.add(CompletableFuture.runAsync(() -> {
            boolean changed = false;
            if (product.getImageUrl() != null && !product.getImageUrl().startsWith("https://res.cloudinary.com")) {
                String newUrl = uploadToCloudinary(product.getImageUrl());
                product.setImageUrl(newUrl);
                changed = true;
            }
            if (product.getThumbnailImageUrl() != null && !product.getThumbnailImageUrl().startsWith("https://res.cloudinary.com")) {
                String newUrl = uploadToCloudinary(product.getThumbnailImageUrl());
                product.setThumbnailImageUrl(newUrl);
                changed = true;
            }
            if (changed) {
                productRepository.save(product);
            }
        }, executor));
    }

    // 상세 이미지 업로드
    List<ProductDetailImage> detailImages = detailImageRepository.findAll();
    for (ProductDetailImage img : detailImages) {
        futures.add(CompletableFuture.runAsync(() -> {
            if (img.getImageUrl() != null && !img.getImageUrl().startsWith("https://res.cloudinary.com")) {
                String newUrl = uploadToCloudinary(img.getImageUrl());
                img.setImageUrl(newUrl);
                detailImageRepository.save(img);
            }
        }, executor));
    }

    // 모든 작업 완료될 때까지 대기
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    executor.shutdown();
}


public String uploadToCloudinary(String imageUrl) {
    // ✅ URL 보정 처리 (//로 시작하거나 http://로 시작할 때)
    if (imageUrl.startsWith("//")) {
        imageUrl = "https:" + imageUrl;
    } else if (imageUrl.startsWith("http://")) {
        imageUrl = imageUrl.replaceFirst("^http:", "https:");
    }

    String url = String.format(UPLOAD_URL, cloudName);

    File tempFile = null;
    try {
        tempFile = File.createTempFile("upload-", ".jpg");

        // ✅ RestTemplate로 이미지 다운로드 (User-Agent 강제)
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "Mozilla/5.0");  // 브라우저인 척

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                imageUrl,
                HttpMethod.GET,
                entity,
                byte[].class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            FileUtils.writeByteArrayToFile(tempFile, response.getBody());
        } else {
            log.error("이미지 다운로드 실패 (응답코드): {}", response);
            throw new RuntimeException("이미지 다운로드 실패");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(tempFile));
        body.add("upload_preset", uploadPreset);
        body.add("api_key", apiKey); // ✅ 추가

        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> uploadRequest = new HttpEntity<>(body, uploadHeaders);

        ResponseEntity<Map> uploadResponse = restTemplate.postForEntity(url, uploadRequest, Map.class);

        if (uploadResponse.getStatusCode() == HttpStatus.OK) {
            return (String) uploadResponse.getBody().get("secure_url");
        } else {
            log.error("Cloudinary 업로드 실패: {}", uploadResponse);
            throw new RuntimeException("Cloudinary 업로드 실패");
        }
    } catch (IOException e) {
        log.error("이미지 다운로드 실패 (IOException): {}", e.getMessage());
        throw new RuntimeException("이미지 다운로드 실패", e);
    } finally {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }
}

    
}
