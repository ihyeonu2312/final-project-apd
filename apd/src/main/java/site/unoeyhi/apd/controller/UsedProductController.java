package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import site.unoeyhi.apd.dto.usedproduct.UsedProductResponseDto;

import site.unoeyhi.apd.dto.usedproduct.UsedProductCreateRequestDto;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.UsedProduct;
import site.unoeyhi.apd.entity.UsedProductImage;
import site.unoeyhi.apd.service.MemberService;
import site.unoeyhi.apd.service.UsedProductService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/used-products")
@RequiredArgsConstructor
@Log4j2
public class UsedProductController {

    private final UsedProductService usedProductService;
    private final MemberService memberService; // 판매자 정보 가져올 용도

    // ✅ 상품 등록
@PostMapping
public ResponseEntity<?> createProduct(
        @RequestBody UsedProductCreateRequestDto dto,
        Authentication authentication) {

    // 1️⃣ 로그인 확인
    if (authentication == null || authentication.getName() == null) {
        return ResponseEntity.status(401).body("로그인 필요");
    }

    // 2️⃣ 사용자 조회 (이메일 or 카카오 ID)
    String subject = authentication.getName();
    Optional<Member> memberOpt = subject.contains("@")
            ? memberService.findByEmail(subject)
            : memberService.findByKakaoId(Long.parseLong(subject));

    if (memberOpt.isEmpty()) {
        return ResponseEntity.status(404).body("회원 정보를 찾을 수 없습니다.");
    }

    Member seller = memberOpt.get();

    // 3️⃣ DTO → UsedProduct
    UsedProduct product = UsedProduct.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .condition(UsedProduct.Condition.valueOf(dto.getCondition()))
            .status(UsedProduct.Status.valueOf(dto.getStatus()))
            .seller(seller)
            .build();

    // 4️⃣ 이미지 URL → UsedProductImage로 변환
    List<UsedProductImage> images = dto.getImageUrls().stream()
            .map(url -> UsedProductImage.builder()
                    .imageUrl(url)
                    .usedProduct(product) // 연관관계 설정
                    .build())
            .toList();

    product.setImages(images); // 상품에 이미지 연동

    // 5️⃣ 저장
    UsedProduct saved = usedProductService.createProduct(product);

    return ResponseEntity.ok(saved); // 나중에 DTO 응답으로 바꿔도 됨
}


    // ✅ 상품 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<UsedProductResponseDto> getProduct(@PathVariable Integer id) {
        return usedProductService.findById(id)
                .map(product -> {
                    // 🔥 여기서 DTO로 변환
                    UsedProductResponseDto dto = new UsedProductResponseDto(
                        product.getUsedProductId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getCondition().name(),
                        product.getStatus().name(),
                        product.getSeller().getNickname(),
                        product.getImages().stream()
                            .map(img -> img.getImageUrl())
                            .toList()
                    );
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    

    // ✅ 전체 상품 목록 조회
    @GetMapping
    public ResponseEntity<List<UsedProductResponseDto>> getAllProducts() {
        List<UsedProductResponseDto> products = usedProductService.findAllDtos(); // ✅ DTO로 응답
        return ResponseEntity.ok(products);
    }

    // ✅ 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer id) {
        usedProductService.deleteById(id);
        return ResponseEntity.ok("상품 삭제 완료");
    }
}
