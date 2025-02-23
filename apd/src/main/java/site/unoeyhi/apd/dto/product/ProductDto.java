package site.unoeyhi.apd.dto.product;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private String name;                 // 상품명
    private Double price;                // 가격
    private Integer stockQuantity;       // 재고 수량
    private Long categoryId;             // 카테고리 ID
    private String imageUrl;             // 상품 이미지 URL
    private String thumbnailImageUrl;    // 썸네일 이미지 URL
    private String detailUrl;            // 상세 페이지 URL
    private List<String> additionalImages; // ✅ 추가 이미지 리스트
    private List<OptionDto> options; // ✅ 옵션 리스트 추가
}
