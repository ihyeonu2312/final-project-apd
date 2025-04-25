package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.unoeyhi.apd.service.CloudinaryUploadService;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevToolController {

    private final CloudinaryUploadService cloudinaryUploadService;

    @PostMapping("/upload-cloudinary")
    public String uploadSingleImage(@RequestBody Map<String, String> request) {
        String imageUrl = request.get("imageUrl");
        return cloudinaryUploadService.uploadToCloudinary(imageUrl);
    }
}
