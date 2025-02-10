package site.unoeyhi.apd.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.service.CartService;
import site.unoeyhi.apd.service.MemberService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
  
  private final CartService cartService;
  private final MemberService  memberService;

  @PostMapping("/add")
  public ResponseEntity<String> addItemToCart(@RequestParam Long memberId, @RequestParam Long productId, @RequestParam int quantity) {
      Member member = memberService.findById(memberId)
              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
  
      cartService.addItemCart(member, productId, quantity);
      return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<List<CartItem>> getCartItems(@PathVariable Long memberId) {
      Member member = memberService.findById(memberId)
              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
      
      return ResponseEntity.ok(cartService.getCartItems(member));
  }
}