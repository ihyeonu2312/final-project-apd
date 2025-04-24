package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.service.CloudinaryUploadService;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevToolController {

    private final CloudinaryUploadService cloudinaryUploadService;

    @PostMapping("/upload-cloudinary")
    public String triggerCloudinaryUpload() {
        cloudinaryUploadService.uploadAndUpdateImages();
        return "✅ Cloudinary 업로드 및 DB 업데이트 완료!";
    }
}
