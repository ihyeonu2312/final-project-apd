package site.unoeyhi.apd.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long categoryId;   // ✅ 기본 키
    private String categoryName;       // ✅ 카테고리명
    private String coupangCategoryId; // DB의 coupang_category_id 컬럼
    private String url; // DB의 url 컬럼
}