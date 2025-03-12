package site.unoeyhi.apd.service.cart;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.repository.cart.CartItemRepository;
import site.unoeyhi.apd.repository.cart.CartRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Override
    public void addToCart(Long memberId, Long productId, int quantity) {
        // 1. 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 2. 상품 정보 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // 3. 해당 회원의 장바구니가 있는지 확인
        Cart cart = cartRepository.findByMember(member)
                .orElseGet(() -> {
                    // 장바구니가 없으면 새로 생성
                    Cart newCart = new Cart();
                    newCart.setMember(member);
                    return cartRepository.save(newCart);
                });

        // 4. 장바구니에 해당 상품이 이미 있는지 확인
        Optional<CartItem> cartItemOpt = cartItemRepository.findByCartAndProduct(cart, product);

        if (cartItemOpt.isPresent()) {
            // 이미 존재하는 상품이면 수량 증가
            CartItem cartItem = cartItemOpt.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            // 새 상품 추가
            CartItem newCartItem = new CartItem();
            newCartItem.setCart(cart);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            newCartItem.setPrice(product.getPrice()); // 상품 가격 저장
            cartItemRepository.save(newCartItem);
        }
    }
}
