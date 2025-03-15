package site.unoeyhi.apd.dto.cart;

import lombok.Getter;
import lombok.Setter;

/**
 * 새로운 주문을 요청할 때 사용되는 DTO
 */
@Getter
@Setter
public class OrderRequestDto {
    private Long memberId;  // ✅ 주문할 회원의 ID
}
