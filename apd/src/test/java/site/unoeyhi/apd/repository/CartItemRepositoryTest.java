package site.unoeyhi.apd.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.product.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Rollback(false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartItemRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    private Member member;
    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setup() {
        // ✅ 회원 생성 및 저장
        member = new Member();
        member.setMemberId(1L);
        member.setNickname("이상원");
        memberRepository.save(member);

        // ✅ 장바구니 생성 및 저장
        cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);

        // ✅ 상품 생성 및 저장
        product = new Product();
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setStockQuantity(10);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        // ✅ 장바구니 아이템 생성 및 저장
        cartItem = new CartItem(cart, product, 2);
        cartItemRepository.save(cartItem);
    }

    @Test
    @DisplayName("✅ 장바구니 아이템 저장 및 조회 테스트")
    void testCartItemCreation() {
        // ✅ when: 장바구니 아이템을 ID로 조회 (cartItemId가 필요)
        Long cartItemId = 1L; // 테스트용 장바구니 아이템 ID 설정
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartItemId);

        // ✅ then: 장바구니에 아이템이 정상적으로 추가되었는지 확인
        assertThat(optionalCartItem).isPresent(); // Optional이 비어있지 않은지 확인
        CartItem cartItem = optionalCartItem.get(); // CartItem 객체 가져오기
        assertThat(cartItem.getCart()).isEqualTo(cart); // Cart 객체와 비교
    }

} 
