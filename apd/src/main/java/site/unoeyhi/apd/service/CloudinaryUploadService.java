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
import java.util.List;
import java.util.Map;

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
        // 대표 이미지 처리
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
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
                productRepository.save(product); // 🔥 변경된 경우에만 저장
            }
        }
    
        // 상세 이미지 처리
        List<ProductDetailImage> detailImages = detailImageRepository.findAll();
        for (ProductDetailImage img : detailImages) {
            if (img.getImageUrl() != null && !img.getImageUrl().startsWith("https://res.cloudinary.com")) {
                String newUrl = uploadToCloudinary(img.getImageUrl());
                img.setImageUrl(newUrl);
                detailImageRepository.save(img); // 🔥 저장
            }
        }
    }

    public String uploadToCloudinary(String imageUrl) {
        String url = String.format(UPLOAD_URL, cloudName);
    
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload-", ".jpg");
            FileUtils.copyURLToFile(new URL(imageUrl), tempFile);
    
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));
            body.add("upload_preset", uploadPreset);
            body.add("api_key", apiKey);  // ✅ 추가
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
    
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
    
            if (response.getStatusCode() == HttpStatus.OK) {
                return (String) response.getBody().get("secure_url");
            } else {
                log.error("Cloudinary 업로드 실패: {}", response);
                throw new RuntimeException("Cloudinary 업로드 실패");
            }
        } catch (IOException e) {
            log.error("이미지 다운로드 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 다운로드 실패", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
}
