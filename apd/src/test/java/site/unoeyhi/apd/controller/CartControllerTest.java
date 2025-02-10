// package site.unoeyhi.apd.controller;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.test.web.servlet.MockMvc;

// import site.unoeyhi.apd.service.CartService;

// @SpringBootTest
// class CartControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private CartService cartService;

//     @Test
//     void addItemToCartTest() throws Exception {
//         mockMvc.perform(post("/api/cart/add")
//                         .param("productId", "1")
//                         .param("quantity", "2"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("Item added to cart"));
//     }
// }