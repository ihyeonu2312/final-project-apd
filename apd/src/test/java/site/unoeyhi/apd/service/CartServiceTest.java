// package site.unoeyhi.apd.service;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Transactional;

// import lombok.extern.log4j.Log4j2;
// import site.unoeyhi.apd.dto.cart.CartRequestDto;
// import site.unoeyhi.apd.entity.Cart;
// import site.unoeyhi.apd.entity.CartItem;
// import site.unoeyhi.apd.entity.Category;
// import site.unoeyhi.apd.entity.Member;
// import site.unoeyhi.apd.entity.Product;
// import site.unoeyhi.apd.repository.CategoryRepository;
// import site.unoeyhi.apd.repository.cart.CartItemRepository;
// import site.unoeyhi.apd.repository.cart.CartRepository;
// import site.unoeyhi.apd.repository.product.ProductRepository;
// import site.unoeyhi.apd.service.cart.CartService;

// import static org.assertj.core.api.Assertions.assertThat;

// @Log4j2
// @SpringBootTest
// public class CartServiceTest {

//     @Autowired
//     private CartService cartService;

//     @Autowired
//     private MemberService memberService;

//     @Autowired
//     private CartRepository cartRepository;

//     @Autowired
//     private CartItemRepository cartItemRepository;

//     @Autowired
//     private ProductRepository productRepository;

//     @Autowired
//     private CategoryRepository categoryRepository;

//     private Product product;
//     private Member member;

//     @Transactional
//     @BeforeEach
//     public void setup() {
//         // ✅ 회원 설정
//         member = new Member();
//         member.setMemberId(8L);  // ✅ ID를 8번으로 설정
//         member.setNickname("testNick");
//         memberService.save(member);
    
//         // ✅ 카테고리 및 상품 설정
//         Category category = new Category();
//         category.setCategoryName("패션");
//         categoryRepository.save(category);
    
//         product = new Product();
//         product.setName("Test Product");
//         product.setPrice(100.0);
//         product.setStockQuantity(10);
//         product.setCategory(category);
//         product.setCreatedAt(LocalDateTime.now());
//         product.setUpdatedAt(LocalDateTime.now());
//         productRepository.save(product);

//          // ✅ 장바구니 생성 추가
//         Cart cart = new Cart();
//         cart.setMember(member);
//         cartRepository.save(cart);
//     }
    
//     @Transactional
//     @Test
//     public void testAddProductWithCategory() {
//         // ✅ when: 상품을 조회하고 카테고리가 제대로 설정되었는지 확인
//         Product foundProduct = productRepository.findById(product.getProductId()).orElseThrow();
    
//         // ✅ then: 카테고리 이름을 문자열로 비교
//         assertThat(foundProduct.getCategory().getCategoryName()).isEqualTo("패션");
//     }
    
//     @Transactional
//     @Test
//     public void testAddItemToCart() {
//         Long productId = product.getProductId();
//         int quantity = 2;

//         // ✅ 장바구니 조회
//         Optional<Cart> optionalCart = cartRepository.findByMember_MemberId(member.getMemberId());
//         if (optionalCart.isEmpty()) {
//             throw new IllegalArgumentException("장바구니가 없습니다.");
//         }
//         Cart cart = optionalCart.get();

//         // ✅ CartItemDto 생성
//         CartRequestDto cartRequestDto = new CartRequestDto();
//         cartRequestDto.setProductId(productId);
//         cartRequestDto.setQuantity(quantity);


//         // ✅ 장바구니에 상품 추가
//         cartService.addItemToCart(cartRequestDto); // 메서드명 수정

//         // ✅ CartItem 조회
//         List<CartItem> items = cartItemRepository.findByCart(cart);

//         // ✅ 검증
//         assertThat(items).hasSize(1);
//         assertThat(items.get(0).getProduct()).isEqualTo(product);
//         assertThat(items.get(0).getQuantity()).isEqualTo(quantity);
//     }
    
// }    