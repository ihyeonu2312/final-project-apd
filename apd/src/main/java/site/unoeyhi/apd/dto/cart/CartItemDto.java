package site.unoeyhi.apd.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.unoeyhi.apd.entity.CartItem;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    @NotNull(message = "장바구니 ID는 필수입니다.")
    private Long cartId;

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @NotNull(message = "수량은 필수입니다.")
    private Integer quantity;

    public static CartItemDto fromEntity(CartItem cartItem) {
        return new CartItemDto(
            cartItem.getCart().getCartId(),
            cartItem.getProduct().getProductId(),
            cartItem.getQuantity()
        );
    }
}


