// package site.unoeyhi.apd.controller;

// import java.util.Optional;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.test.web.servlet.MockMvc;

// import site.unoeyhi.apd.entity.Member;
// import site.unoeyhi.apd.service.CartService;
// import site.unoeyhi.apd.service.MemberService;

// @WebMvcTest(CartController.class)
// public class CartControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private CartService cartService;

//     @MockBean
//     private MemberService memberService;

//     @Test
//     public void testAddItemToCart() throws Exception {
//         Long memberId = 1L;
//         Long productId = 100L;
//         int quantity = 2;

//         Member member = new Member();
//         member.setMemberId(memberId);
//         member.setNickname("testUser");

//         // memberService가 memberId로 Member를 반환하도록 설정
//         given(memberService.findById(memberId)).willReturn(Optional.of(member));

//         // mockMvc로 POST 요청 시뮬레이션
//         mockMvc.perform(post("/cart/add")
//                 .param("memberId", String.valueOf(memberId))
//                 .param("productId", String.valueOf(productId))
//                 .param("quantity", String.valueOf(quantity)))
//                 .andExpect(status().isOk())  // HTTP 상태 200 OK 확인
//                 .andExpect(content().string("상품이 장바구니에 추가되었습니다."));  // 응답 내용 확인
//     }

//     @Test
//     public void testGetCartItems() throws Exception {
//         Long memberId = 1L;

//         Member member = new Member();
//         member.setId(memberId);
//         member.setNickname("testUser");

//         Cart cart = new Cart();
//         cart.setMember(member);

//         CartItem item = new CartItem();
//         item.setCart(cart);
//         item.setProductId(100L);
//         item.setQuantity(2);

//         // service의 getCartItems가 장바구니 아이템을 반환하도록 설정
//         given(cartService.getCartItems(member)).willReturn(List.of(item));

//         // mockMvc로 GET 요청 시뮬레이션
//         mockMvc.perform(get("/cart/{memberId}", memberId))
//                 .andExpect(status().isOk())  // HTTP 상태 200 OK 확인
//                 .andExpect(jsonPath("$[0].productId").value(100L))  // JSON 내용 확인
//                 .andExpect(jsonPath("$[0].quantity").value(2));
//     }
// }