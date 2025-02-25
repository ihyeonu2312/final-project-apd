package site.unoeyhi.apd.dto.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionDto {
    private String optionValueType; // ✅ 옵션 유형 (예: COLOR, SIZE, DEFAULT)
    private String optionValue;     // ✅ 옵션 값 (예: 빨강, 파랑, M, L)
    private String optionImageUrl;  // ✅ 옵션 이미지 URL (있을 경우)

    // 기존 생성자 유지 (이미지가 없는 경우를 위한 생성자)
    public OptionDto(String optionValueType, String optionValue) {
        this.optionValueType = optionValueType;
        this.optionValue = optionValue;
        this.optionImageUrl = null; // 기본값
    }
}
