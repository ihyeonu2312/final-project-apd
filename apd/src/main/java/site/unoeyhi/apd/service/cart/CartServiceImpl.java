package site.unoeyhi.apd.service.cart;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.CartResponseDto;
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
            System.out.println("🚀 장바구니가 존재하지 않아 새로 생성합니다.");
            Cart newCart = new Cart();
            newCart.setMember(member);  // ✅ member 설정 추가
            newCart.setCreatedAt(LocalDateTime.now());
            return cartRepository.save(newCart);
        });
        System.out.println("✅ Cart 조회 성공: " + cart.getCartId());

        

        // 4. 장바구니에 해당 상품이 이미 있는지 확인
        Optional<CartItem> cartItemOpt = cartItemRepository.findByCartAndProduct(cart, product);

        if (cartItemOpt.isPresent()) {
            CartItem cartItem = cartItemOpt.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            CartItem newCartItem = new CartItem(cart, product, quantity, product.getPrice());
            cartItemRepository.save(newCartItem);
        }
    }

    /** 🛒 장바구니 조회 */
    @Override
    public CartResponseDto getCart(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // ✅ 장바구니 조회
        Cart cart = cartRepository.findByMember(member)
            .orElseGet(() -> {
                System.out.println("🚀 장바구니가 존재하지 않아 새로 생성합니다.");
                Cart newCart = new Cart();
                newCart.setMember(member);
                newCart.setCreatedAt(LocalDateTime.now());
                return cartRepository.save(newCart);
            });

        System.out.println("✅ Cart 조회 성공: " + cart.getCartId());

        // ✅ 기존 생성 방식 → `fromEntity` 메서드로 변경하여 `memberId` 포함
        return CartResponseDto.fromEntity(cart);
    }

    /** 🛒 특정 상품 장바구니에서 제거 */
    @Override
    public void removeFromCart(Long memberId, Long productId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Cart cart = cartRepository.findByMember(member)
            .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
            .orElseThrow(() -> new IllegalArgumentException("장바구니에 해당 상품이 존재하지 않습니다."));
        cartItemRepository.delete(cartItem);
    }

    /** 🛒 장바구니 비우기 */
    @Override
    public void clearCart(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Optional<Cart> cartOpt = cartRepository.findByMember(member);
        if (cartOpt.isEmpty()) {
            System.out.println("⚠️ 장바구니 없음: memberId = " + memberId);
            return; // 장바구니가 없으면 그냥 리턴
        }

        Cart cart = cartRepository.findByMember(member)
            .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));
        cartItemRepository.deleteAllByCart(cart);
    }

    /** 🛒 장바구니 상품 수량 변경 */
    @Override
    public void updateQuantity(Long memberId, Long productId, int quantity) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Cart cart = cartRepository.findByMember(member)
            .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
            .orElseThrow(() -> new IllegalArgumentException("장바구니에 해당 상품이 존재하지 않습니다."));
        if (quantity > 0) {
            cartItem.setQuantity(quantity);
        } else {
            cartItemRepository.delete(cartItem);
        }
    }
}
