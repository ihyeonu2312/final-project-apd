package site.unoeyhi.apd.dto.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionDto {
    private String optionValueType; // ✅ 필드명 수정 (optionName → optionValueType)
    private String optionValue;     // ✅ 옵션 값 (예: 빨강, 파랑, M, L)
}
