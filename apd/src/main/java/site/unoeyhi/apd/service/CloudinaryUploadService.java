package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.entity.ProductDetailImage;
import site.unoeyhi.apd.repository.product.ProductDetailImageRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryUploadService {

    private final ProductRepository productRepository;
    private final ProductDetailImageRepository detailImageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

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
            if (product.getImageUrl() != null && !product.getImageUrl().startsWith("https://res.cloudinary.com")) {
                String newUrl = uploadToCloudinary(product.getImageUrl());
                product.setImageUrl(newUrl);
            }
            if (product.getThumbnailImageUrl() != null && !product.getThumbnailImageUrl().startsWith("https://res.cloudinary.com")) {
                String newUrl = uploadToCloudinary(product.getThumbnailImageUrl());
                product.setThumbnailImageUrl(newUrl);
            }
        }

        // 상세 이미지 처리
        List<ProductDetailImage> detailImages = detailImageRepository.findAll();
        for (ProductDetailImage img : detailImages) {
            if (img.getImageUrl() != null && !img.getImageUrl().startsWith("https://res.cloudinary.com")) {
                String newUrl = uploadToCloudinary(img.getImageUrl());
                img.setImageUrl(newUrl);
            }
        }
    }

    private String uploadToCloudinary(String imageUrl) {
        String url = String.format(UPLOAD_URL, cloudName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageUrl);
        body.add("upload_preset", uploadPreset);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return (String) response.getBody().get("secure_url");
        } else {
            log.error("Cloudinary 업로드 실패: {}", response);
            throw new RuntimeException("Cloudinary 업로드 실패");
        }
    }
}
