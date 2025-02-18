package site.unoeyhi.apd.entity.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long adminId;         // ✅ 관리자 ID
    private String name;          // ✅ 상품 이름
    private String description;   // ✅ 상품 설명
    private Double price;         // ✅ 상품 가격
    private Integer stockQuantity; // ✅ 재고 수량
    private Long categoryId;  // ✅ 카테고리 ID (FK)
    private String imageUrl;  // ✅ 이미지 URL 추가
}
