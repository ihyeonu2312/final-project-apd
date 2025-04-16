package site.unoeyhi.apd.dto.product;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryWithProductsDto {
    private Long categoryId;  // ✅ 카테고리 ID
    private String categoryName;  // ✅ 카테고리 이름
    private String url;  // ✅ URL
    List<ProductSummaryDto> products;  // ✅ 엔티티 대신 DTO
}
