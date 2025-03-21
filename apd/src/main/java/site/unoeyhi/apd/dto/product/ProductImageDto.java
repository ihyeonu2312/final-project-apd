package site.unoeyhi.apd.dto.product;

import lombok.*;
import site.unoeyhi.apd.entity.ProductImage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto {
    private Long imageId;       
    private Long productId;     
    private String imageUrl;  // ✅ 기존 `imageUrl` 필드 추가
    private String smallImageUrl; 
    private String largeImageUrl; 
    private boolean isThumbnail;  

    // ✅ 엔티티 → DTO 변환
    public static ProductImageDto fromEntity(ProductImage productImage) {
        return ProductImageDto.builder()
                .imageId(productImage.getImageId())
                .productId(productImage.getProduct().getProductId())
                .imageUrl(productImage.getImageUrl()) // ✅ 추가
                .smallImageUrl(productImage.getImageUrl()) 
                .largeImageUrl(productImage.getLargeImageUrl()) 
                .isThumbnail(productImage.isThumbnail())
                .build();
    }
}
