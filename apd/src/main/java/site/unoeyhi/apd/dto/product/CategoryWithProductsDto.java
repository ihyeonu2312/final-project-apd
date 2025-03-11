package site.unoeyhi.apd.dto.product;

import lombok.*;
import site.unoeyhi.apd.entity.Product;
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
    private List<Product> products;  // ✅ 상품 목록 포함
}
