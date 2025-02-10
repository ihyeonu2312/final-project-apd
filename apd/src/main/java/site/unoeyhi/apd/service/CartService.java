package site.unoeyhi.apd.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CartItemRepository;
import site.unoeyhi.apd.repository.CartRepository;
import site.unoeyhi.apd.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  // public Cart getCart(Member member){ //단일 조회
  // return cartRepository.findByMember(member)
  // .orElseGet(() -> {
  // Cart newCart = new Cart();
  // newCart.setMember(member);
  // newCart.setCreatedAt(LocalDateTime.now());
  // return cartRepository.save(newCart);
  // });
  // }
  public List<Cart> getAllCart(Member member) {
    List<Cart> carts = cartRepository.findByMember(member);
    if (carts.isEmpty()) {
      // 카트가 없다면 새 카트를 생성
      Cart newCart = new Cart();
      newCart.setMember(member);
      newCart.setCreatedAt(LocalDateTime.now());
      cartRepository.save(newCart);
      carts.add(newCart);
    }

    return carts; // 여러 카트를 반환
  }

  // 회원의 장바구니 아이템 목록 반환
  public List<CartItem> getCartItems(Member member) {
    List<Cart> carts = getAllCart(member); // 회원의 장바구니 리스트 가져오기
    if (carts.isEmpty()) {
      return List.of(); // 카트가 없으면 빈 리스트 반환
    }
     List<CartItem> allCartItems = new ArrayList<>();
    // 모든 카트를 순회하면서 각 카트에 포함된 아이템들을 가져와 추가
    for (Cart cart : carts) {
        allCartItems.addAll(cartItemRepository.findByCart(cart)); // 각 카트에 속한 모든 아이템 추가
    }

    return allCartItems; // 모든 카트의 아이템들을 반환
  }

  public void addItemCart(Member member, Long productId, int quantity) {

    // 상품 조회
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

    // 장바구니 가져오기, 장바구니 생성
    List<Cart> carts = getAllCart(member);

    // carts가 비어있다면 return
    if (carts.isEmpty()) {
      throw new IllegalArgumentException("장바구니가 없습니다.");
    }

    // 이미 존재하는 상품인지 확인
    Optional<CartItem> existingItem = carts.get(0).getCartItems().stream()
        .filter(item -> item.getProduct().equals(product))
        .findFirst();

    // 만약 하나 이상의 상품이 일치한다면 처리
    if (existingItem.isPresent()) {
      // 처리 로직 (예: 첫 번째 일치하는 상품만 처리)
      CartItem itemToUpdate = existingItem.get();
      itemToUpdate.setQuantity(itemToUpdate.getQuantity() + quantity); // 수량 업데이트
      itemToUpdate.setUpdatedAt(LocalDateTime.now()); // 수정 시간 갱신
      cartItemRepository.save(itemToUpdate); // 아이템 저장
    } else {
      // 일치하는 상품이 없다면 새로운 아이템 추가 로직
      CartItem newItem = new CartItem();
      newItem.setCart(carts.get(0));// 첫번째 카트에 아이템 추가
      newItem.setProduct(product);
      newItem.setQuantity(quantity);
      newItem.setCreatedAt(LocalDateTime.now());
      carts.get(0).getCartItems().add(newItem);// 카트아이템 추가
      cartItemRepository.save(newItem); // 아이템 저장
    }
  }

}
