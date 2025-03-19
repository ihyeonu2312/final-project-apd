package site.unoeyhi.apd.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private Long createdAt;
    
    // ✅ `Cart` 엔터티를 DTO로 변환하는
    public static CartResponseDto fromEntity(Cart cart) {
        return new CartResponseDto(
            cart.getCartId(),
            cart.getMember() != null ? cart.getMember().getMemberId() : null, // ✅ `memberId`가 null이 아닐 경우 가져오기
            cart.getCartItems() != null 
                ? cart.getCartItems().stream().map(CartItemDto::fromEntity).collect(Collectors.toList()) // ✅ CartItem 변환
                : new ArrayList<>(),
            cart.getCreatedAt() != null ? cart.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null
        );
    }
}