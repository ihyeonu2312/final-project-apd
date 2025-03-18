package site.unoeyhi.apd.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;

import java.time.LocalDateTime;
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
    
    // ✅ 추가: CartResponseDto에 맞는 생성자 정의
    public CartResponseDto(Long id, Long createdAt, List<CartItemDto> items) {
        this.cartId = id;
        this.createdAt = createdAt;
        this.cartItems = items;
    }

    // // ✅ Entity -> DTO 변환 메서드 수정
    // public static CartResponseDto fromEntity(Cart cart) {
    //     if (cart == null) {
    //         System.out.println("🚨 CartResponseDto.fromEntity - cart가 null입니다!");
    //         return new CartResponseDto(0L, LocalDateTime.now(), new ArrayList<>());  // 🛠 오류 방지
    //     }

    //     return new CartResponseDto(
    //         cart.getCartId(),
    //         cart.getCreatedAt(),
    //         cart.getCartItems() != null ? cart.getCartItems().stream().map(CartItemDto::fromEntity).toList() : new ArrayList<>()
    //     );
    // }
}