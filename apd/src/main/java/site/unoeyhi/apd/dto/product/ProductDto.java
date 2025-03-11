package site.unoeyhi.apd.dto.product;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProductDto {
    private Long productId;
    private String name;                 // 상품명
    private Double price;                // 가격
    private Double discountPrice;        // 할인 가격
    private Integer stockQuantity;       // 재고 수량
    private Long categoryId;             // 카테고리 ID
    private String imageUrl;             // 상품 이미지 URL
    private String thumbnailImageUrl;    // 썸네일 이미지 URL
    private Double rating;
    private String detailUrl;            // 상세 페이지 URL
    private List<String> additionalImages; // ✅ 추가 이미지 리스트
    private List<OptionDto> options; // ✅ 옵션 리스트 추가

    public ProductDto(Long productId, String name, Double price, String thumbnailImageUrl, Double rating) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.rating = rating != null ? rating : 0.0;  // ⭐ null 값 방지
    }
}


