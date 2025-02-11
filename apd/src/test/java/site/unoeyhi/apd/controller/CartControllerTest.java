package site.unoeyhi.apd.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.service.CartService;
import site.unoeyhi.apd.service.MemberService;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Mock
    private MemberService memberService;

     @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void addItemToCartTest() throws Exception {
        Long memberId = 1L;
        Long productId = 1L;
        int quantity = 2;

        // Mock 데이터 설정
        Member mockMember = new Member();
        mockMember.setMemberId(memberId);
        Mockito.when(memberService.findById(memberId)).thenReturn(Optional.of(mockMember));

        Mockito.doNothing().when(cartService).addItemCart(mockMember, productId, quantity);

        // API 호출 및 검증
        mockMvc.perform(post("/api/cart/add")
                        .with(csrf())  // CSRF 토큰 추가
                        .param("memberId", String.valueOf(memberId))
                        .param("productId", String.valueOf(productId))
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk())  // 응답 상태가 200 OK인지 확인
                .andExpect(content().string("상품이 장바구니에 추가되었습니다."))
                .andDo(print());
    }
}
