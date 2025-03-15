package site.unoeyhi.apd.service.cart;

import site.unoeyhi.apd.dto.cart.CartResponseDto;

public interface CartService {
     /** 장바구니에 상품 추가 */
     void addToCart(Long memberId, Long productId, int quantity);

     /** 특정 회원의 장바구니 조회 */
     CartResponseDto getCart(Long memberId);
 
     /** 장바구니에서 특정 상품 제거 */
     void removeFromCart(Long memberId, Long productId);
 
     /** 장바구니 비우기 */
     void clearCart(Long memberId);
 
     /** 장바구니 상품 수량 업데이트 */
     void updateQuantity(Long memberId, Long productId, int quantity);
 }
