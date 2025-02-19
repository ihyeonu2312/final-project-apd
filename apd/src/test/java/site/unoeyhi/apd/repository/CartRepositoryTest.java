package site.unoeyhi.apd.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.Member;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Rollback(false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private Cart cart;

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
    }

    @Test
    @DisplayName("✅ 장바구니 저장 및 회원 연결 테스트")
    void testCartCreation() {
        // ✅ when: 장바구니 조회
        Optional<Cart> optionalCart = cartRepository.findByMember_MemberId(member.getMemberId());

        // ✅ then: Optional 사용법 수정
        assertThat(optionalCart).isPresent(); // ✅ Optional이 비어있지 않은지 확인
        Cart cart = optionalCart.get(); // ✅ 실제 Cart 객체 가져오기
        assertThat(cart.getMember()).isEqualTo(member);
    }

}
