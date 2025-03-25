package site.unoeyhi.apd.dto.usedproduct;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UsedProductCreateRequestDto {
    private String name;
    private String description;
    private BigDecimal price;
    private String condition; // 새상품, 최상, 상, 중, 하
    private String status;    // 판매중, 예약중, 거래완료
    private List<String> imageUrls; // 이미지 URL 리스트
}
