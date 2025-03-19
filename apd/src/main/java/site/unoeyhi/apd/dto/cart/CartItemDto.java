package site.unoeyhi.apd.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Product;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    @NotNull(message = "장바구니 ID는 필수입니다.")
    private Long cartId;

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @NotNull(message = "상품명은 필수입니다.")
    private String productName; // ✅ 상품 이름 추가

    @NotNull(message = "상품 이미지 URL은 필수입니다.")
    private String imageUrl; // ✅ 상품 이미지 추가

    @NotNull(message = "상품 가격은 필수입니다.")
    private double price; // ✅ `int`로 변경

    @NotNull(message = "수량은 필수입니다.")
    private Integer quantity;

    public static CartItemDto fromEntity(CartItem cartItem) {
        Product product = cartItem.getProduct();
        return new CartItemDto(
            cartItem.getCart().getCartId(),
            product.getProductId(),
            product.getName() != null ? product.getName() : "이름 없음", // ✅ 상품 이름이 없을 경우 기본값
            product.getImageUrl() != null ? product.getImageUrl() : "https://via.placeholder.com/150", // ✅ 상품 이미지 없을 경우 기본값
            product.getPrice(),
            cartItem.getQuantity()
        );
    }
}


