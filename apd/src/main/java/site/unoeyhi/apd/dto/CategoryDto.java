package site.unoeyhi.apd.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long categoryId;   // ✅ 기본 키
    private String categoryKey;   
    private String name;       // ✅ 카테고리명
    private String url;        // ✅ 카테고리 URL
    private String categoryName; // ✅ AliExpress 원본 카테고리명
    private String categoryUrl; // ✅ AliExpress 원본 URL
}
