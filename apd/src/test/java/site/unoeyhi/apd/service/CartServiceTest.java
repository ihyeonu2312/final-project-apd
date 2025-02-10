package site.unoeyhi.apd.service;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Category;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.Product;
import site.unoeyhi.apd.repository.CartItemRepository;
import site.unoeyhi.apd.repository.CartRepository;
import site.unoeyhi.apd.repository.CategoryRepository;
import site.unoeyhi.apd.repository.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
public class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product product;

    @BeforeEach
    public void setup() {
        // given: 회원과 상품 정보 준비
        Member member = new Member();
        member.setMemberId(1L);  // id는 실제 DB에 맞게 설정
        member.setNickname("testUser");
        memberService.save(member);  // save 메서드를 구현해야 함

        // 상품 정보 준비
        product = new Product();
        product.setName("Test Product");
        product.setDescription("This is a test product");
        product.setPrice(100.0);  // 가격 설정
        product.setStockQuantity(10);  // 재고 수량 설정
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product); // 상품 저장 (없다면)
        
    }
    @Test
public void testAddProductWithCategory() {
    // given: 카테고리와 상품 정보 준비
    Category category = new Category();
    category.setName("Electronics"); // 카테고리 이름 설정
    categoryRepository.save(category); // 카테고리 저장

    Product product = new Product();
    product.setName("Test Product");
    product.setDescription("This is a test product");
    product.setPrice(100.0);
    product.setStockQuantity(10);
    product.setCreatedAt(LocalDateTime.now());
    product.setUpdatedAt(LocalDateTime.now());
    product.setCategory(category); // 상품에 카테고리 설정
    productRepository.save(product); // 상품 저장

    // when: 상품을 조회하고 카테고리가 제대로 설정되었는지 확인
    Product foundProduct = productRepository.findById(product.getProductId()).orElseThrow();
    
    // then: 카테고리가 제대로 설정되었는지 확인
    assertThat(foundProduct.getCategory().getName()).isEqualTo("Electronics");
}

    @Test
    public void testAddItemToCart() {
        Long productId = product.getProductId(); // 저장된 상품의 ID 사용
        int quantity = 2;

        Member member = new Member();
        member.setMemberId(1L);  // id는 실제 DB에 맞게 설정
        member.setNickname("testUser");
        memberService.save(member);  // save 메서드를 구현해야 함

        // when: 장바구니에 상품 추가
        cartService.addItemCart(member, productId, quantity);

        // then: 장바구니에 추가된 아이템이 존재하는지 확인
        Cart cart = cartRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 없습니다."));
        List<CartItem> items = cartItemRepository.findByCart(cart);

        assertThat(items).hasSize(1);  // 아이템이 1개여야 한다
        assertThat(items.get(0).getProduct()).isEqualTo(productId);
        assertThat(items.get(0).getQuantity()).isEqualTo(quantity);
    }

    @Test
    public void testGetCartItems() {
        // given: 회원과 장바구니 아이템 준비
        Member member = new Member();
        member.setMemberId(1L);  // id는 실제 DB에 맞게 설정
        member.setNickname("testUser");
        memberService.save(member);  // save 메서드를 구현해야 함

        // Cart 및 CartItem 생성
          Cart cart = new Cart();
          cart.setMember(member);
          cartRepository.save(cart);
  
          // CartItem 생성 및 저장
          CartItem item = new CartItem();
          item.setCart(cart);
          item.setProduct(product);  // setup()에서 생성된 product 사용
          item.setQuantity(2);
          cartItemRepository.save(item);
  
          // when: 장바구니 아이템 조회
          List<CartItem> cartItems = cartService.getCartItems(member);
  
          // then: 장바구니 아이템이 1개 있어야 한다
          assertThat(cartItems).hasSize(1);
          assertThat(cartItems.get(0).getProduct()).isEqualTo(product);  // Product 비교
      }
  }