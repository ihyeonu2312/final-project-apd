package site.unoeyhi.apd.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDto {
    private Long cartId;
    private Long memberId;
    private List<CartItemDto> cartItems;

    public static CartResponseDto fromEntity(Cart cart) {
        return new CartResponseDto(
            cart.getCartId(),
            cart.getMember().getMemberId(),
            cart.getCartItems().stream()
                .map(CartItemDto::fromEntity)
                .collect(Collectors.toList())
        );
    }
}
