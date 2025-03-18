package site.unoeyhi.apd.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.service.cart.CartService;
import site.unoeyhi.apd.dto.cart.*;
import site.unoeyhi.apd.security.CustomUserDetails;

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
        if (userDetails == null) {
            System.out.println("🚨 인증 오류: userDetails가 null임");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 401 Unauthorized 반환
        }

        Long memberId = userDetails.getMemberId();
        System.out.println("✅ 장바구니 요청 - memberId: " + memberId);

        CartResponseDto cartResponse = cartService.getCart(memberId);

       // ✅ 장바구니 데이터가 null인지 확인
        if (cartResponse == null) {
            System.out.println("🚨 CartResponseDto가 null입니다!");
        } else {
            System.out.println("✅ CartResponseDto 응답: " + cartResponse);
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
