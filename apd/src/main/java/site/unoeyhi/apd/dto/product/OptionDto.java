package site.unoeyhi.apd.dto.product;

import lombok.*;
import site.unoeyhi.apd.entity.Option;
import site.unoeyhi.apd.entity.ProductOption;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionDto {
    private Long optionId;          // ✅ 옵션 ID
    private String optionValueType; // ✅ 옵션 유형 (예: COLOR, SIZE)
    private String optionValue;     // ✅ 옵션 값 (예: 빨강, 파랑, M, L)
    private LocalDateTime createdAt; // ✅ 옵션 생성 날짜
    private String optionImageUrl;  // ✅ 옵션 이미지 URL (있을 경우)
    private int priceGap; 

    // ✅ `Option` 엔티티를 DTO로 변환하는 생성자
    public OptionDto(Option option) {
        this.optionId = option.getOptionId();
        this.optionValueType = option.getOptionValueType();
        this.optionValue = option.getOptionValue();
        this.createdAt = option.getCreatedAt();
        this.priceGap = option.getPriceGap();
    }

    // ✅ `ProductOption` 엔티티를 DTO로 변환하는 생성자
    public OptionDto(ProductOption productOption) {
        Option option = productOption.getOption(); // ✅ 옵션 정보 가져오기
        this.optionId = option.getOptionId();
        this.optionValueType = option.getOptionValueType();
        this.optionValue = option.getOptionValue();
        this.createdAt = option.getCreatedAt();
        this.priceGap = productOption.getPriceGap();
    }

    public OptionDto(String optionValueType, String optionValue, int priceGap) {
        this.optionValueType = optionValueType;
        this.optionValue = optionValue;
        this.priceGap = priceGap;
        this.optionImageUrl = null; // ✅ 새로운 옵션 추가 시 기본값
        this.createdAt = LocalDateTime.now(); // 기본 생성 시간 설정
    }
    
}
