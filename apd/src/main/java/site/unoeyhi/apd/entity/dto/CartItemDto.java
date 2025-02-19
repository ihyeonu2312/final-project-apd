package site.unoeyhi.apd.entity.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long cartId;       // 장바구니 ID
    private Long productId;    // 상품 ID
    private Integer quantity;  // 상품 수량
}