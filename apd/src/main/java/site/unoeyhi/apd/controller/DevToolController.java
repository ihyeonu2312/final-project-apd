package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.service.CloudinaryUploadService;

import java.util.Map;
@Log4j2
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevToolController {

    private final CloudinaryUploadService cloudinaryUploadService;

    // 단일 업로드 (기존)
    @PostMapping("/upload-cloudinary")
    public String uploadSingleImage(@RequestBody Map<String, String> request) {
        String imageUrl = request.get("imageUrl");
        log.info("✅ 업로드 요청 받은 imageUrl: {}", imageUrl);
        return cloudinaryUploadService.uploadToCloudinary(imageUrl);
    }

    // 전체 업로드 (새로 추가)
    @PostMapping("/upload-all-cloudinary")
    public String uploadAllImages() {
        cloudinaryUploadService.uploadAndUpdateImages();
        return "✅ 모든 이미지 Cloudinary 업로드 완료";
    }
}
