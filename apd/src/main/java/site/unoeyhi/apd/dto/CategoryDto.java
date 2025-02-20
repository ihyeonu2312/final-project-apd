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
}