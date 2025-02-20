// package site.unoeyhi.apd.service;

// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import site.unoeyhi.apd.entity.Cart;
// import site.unoeyhi.apd.entity.CartItem;
// import site.unoeyhi.apd.entity.Product;
// import site.unoeyhi.apd.entity.dto.CartRequestDto;
// import site.unoeyhi.apd.entity.dto.CartItemDto;
// import site.unoeyhi.apd.repository.CartItemRepository;
// import site.unoeyhi.apd.repository.CartRepository;
// import site.unoeyhi.apd.repository.ProductRepository;

// import java.util.Optional;

// @Service
// public class CartService {

//     private final CartRepository cartRepository;
//     private final CartItemRepository cartItemRepository;
//     private final ProductRepository productRepository;

//     public CartService(CartRepository cartRepository,
//                        CartItemRepository cartItemRepository,
//                        ProductRepository productRepository) {
//         this.cartRepository = cartRepository;
//         this.cartItemRepository = cartItemRepository;
//         this.productRepository = productRepository;
//     }

//     // ✅ 장바구니에 상품 추가 & 응답 반환
//     @Transactional
//     public ResponseEntity<CartItemDto> addItemToCart(CartRequestDto cartRequestDto) {
//         // ✅ 1. 장바구니 찾기
//         Cart cart = cartRepository.findById(cartRequestDto.getProductId())
//                 .orElseThrow(() -> new IllegalArgumentException("장바구니 ID가 존재하지 않습니다."));

//         // ✅ 2. 상품 찾기
//         Product product = productRepository.findById(cartRequestDto.getProductId())
//                 .orElseThrow(() -> new IllegalArgumentException("상품 ID가 존재하지 않습니다."));

//         // ✅ 3. 장바구니에 동일한 상품이 있는지 확인
//         Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);

//         CartItem savedCartItem;
//         if (existingCartItem.isPresent()) {
//             // ✅ 이미 상품이 있다면 수량만 증가
//             CartItem cartItem = existingCartItem.get();
//             cartItem.setQuantity(cartItem.getQuantity() + cartRequestDto.getQuantity());
//             savedCartItem = cartItemRepository.save(cartItem);
//         } else {
//             // ✅ 새로운 장바구니 아이템 생성
//             CartItem cartItem = CartItem.builder()
//                     .cart(cart)
//                     .product(product)
//                     .quantity(cartRequestDto.getQuantity())
//                     .build();
//             savedCartItem = cartItemRepository.save(cartItem);
//         }

//         // ✅ 4. 응답으로 DTO 변환 후 반환
//         CartItemDto responseDto = new CartItemDto(  cart.getCartId().longValue(),   // Integer → Long 변환
//         product.getProductId().longValue(),  // Integer → Long 변환
//         savedCartItem.getQuantity()
//     );
//         return ResponseEntity.ok(responseDto);
//     }
// }
