package site.unoeyhi.apd.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.CartRequestDto;
import site.unoeyhi.apd.service.cart.CartService;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService; // 인터페이스 주입

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody CartRequestDto request) {
        cartService.addToCart(request.getMemberId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
    }
}
