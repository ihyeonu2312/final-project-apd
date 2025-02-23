package site.unoeyhi.apd.dto.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto {
    private Long productId;  // ✅ 상품 ID (연결)
    private String imageUrl; // ✅ 이미지 URL
}
