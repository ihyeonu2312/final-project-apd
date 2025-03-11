package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import site.unoeyhi.apd.dto.product.ReviewDto;
import site.unoeyhi.apd.service.product.ReviewService;

@RestController
@RequestMapping("/api/reviews") // ✅ 기본 URL 설정 확인
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}") // ✅ 상품별 리뷰 목록 조회
    public ResponseEntity<List<ReviewDto>> getReviewsByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
    }

    @GetMapping("/product/{productId}/rating") // ✅ 평균 평점 조회
    public ResponseEntity<Double> getAverageRating(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getAverageRating(productId));
    }

    @PostMapping // ✅ 리뷰 추가
    public ResponseEntity<ReviewDto> addReview(@RequestBody ReviewDto reviewDto) {
        return ResponseEntity.ok(reviewService.addReview(reviewDto));
    }
}

