package site.unoeyhi.apd.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.unoeyhi.apd.entity.ProductDetailImage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailImageDto {
    private Long detailImageId;
    private Long productId;
    private String imageUrl;

    public static ProductDetailImageDto fromEntity(ProductDetailImage entity) {
        return ProductDetailImageDto.builder()
                .detailImageId(entity.getDetailImageId())
                .productId(entity.getProduct().getProductId())
                .imageUrl(entity.getImageUrl())
                .build();
    }
}
