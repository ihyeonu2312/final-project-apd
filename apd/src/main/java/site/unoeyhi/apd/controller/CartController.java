package site.unoeyhi.apd.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.service.cart.CartService;
import site.unoeyhi.apd.dto.cart.*;
import site.unoeyhi.apd.security.CustomUserDetails;
import site.unoeyhi.apd.security.CustomUserDetailsService;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ✅ 장바구니에 상품 추가 (POST)
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody CartRequestDto request) {
        cartService.addToCart(request.getMemberId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
    }

    @GetMapping("")
    public ResponseEntity<CartResponseDto> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        CartResponseDto cartResponse = cartService.getCart(memberId);
        if (cartResponse.getItems().isEmpty()) {
            return ResponseEntity.ok(cartResponse); // 빈 장바구니 응답
        }
        return ResponseEntity.ok(cartResponse);
    }


    // ✅ 장바구니에서 상품 삭제 (DELETE)
    @DeleteMapping("/{memberId}/remove/{productId}")
    public ResponseEntity<String> removeFromCart(@PathVariable Long memberId, @PathVariable Long productId) {
        cartService.removeFromCart(memberId, productId);
        return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");
    }

    // ✅ 장바구니 전체 비우기 (DELETE)
    @DeleteMapping("/{memberId}/clear")
    public ResponseEntity<String> clearCart(@PathVariable Long memberId) {
        cartService.clearCart(memberId);
        return ResponseEntity.ok("장바구니가 비워졌습니다.");
    }

    // ✅ 장바구니 상품 수량 수정 (PATCH)
    @PatchMapping("/{memberId}/update")
    public ResponseEntity<String> updateQuantity(@RequestBody CartRequestDto request) {
        cartService.updateQuantity(request.getMemberId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok("상품 수량이 변경되었습니다.");
    }
}
