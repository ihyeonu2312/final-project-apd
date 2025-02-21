// package site.unoeyhi.apd.service;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import lombok.RequiredArgsConstructor;
// import site.unoeyhi.apd.entity.Cart;
// import site.unoeyhi.apd.entity.CartItem;
// import site.unoeyhi.apd.entity.Member;
// import site.unoeyhi.apd.entity.Product;
// import site.unoeyhi.apd.repository.CartItemRepository;
// import site.unoeyhi.apd.repository.CartRepository;
// import site.unoeyhi.apd.repository.product.ProductRepository;

// @Service
// @RequiredArgsConstructor
// @Transactional
// public class CartServiceImpl implements CartService {

//     private final CartRepository cartRepository;
//     private final CartItemRepository cartItemRepository;
//     private final ProductRepository productRepository;

//     @Override
//     public List<Cart> getAllCart(Member member) {
//         List<Cart> carts = cartRepository.findByMember(member);
//         if (carts.isEmpty()) {
//             Cart newCart = new Cart();
//             newCart.setMember(member);
//             newCart.setCreatedAt(LocalDateTime.now());
//             cartRepository.save(newCart);
//             return new ArrayList<>(List.of(newCart));

//         }
//         return carts;
//     }

//     @Override
//     public Cart getCartForMember(Member member) {
//         // List<Cart> carts = getAllCart(member);
//         // if (carts.isEmpty()) {
//         //     throw new RuntimeException("카트가 존재하지 않습니다.");
//         // }
//         return getAllCart(member).get(0);
//     }

//     @Override
//     @Transactional
//     public List<CartItem> getCartItems(Member member) {
//         List<Cart> carts = getAllCart(member);
//         if (carts.isEmpty()) {
//             return List.of();
//         }
//         Cart cart = carts.get(0);
//         return new ArrayList<>(cart.getCartItems());
//     }

//     @Override
//     public void addItemCart(Member member, Long productId, int quantity) {
//         Product product = productRepository.findById(productId)
//             .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

//         List<Cart> carts = getAllCart(member);
//         if (carts.isEmpty()) {
//             throw new IllegalArgumentException("장바구니가 없습니다.");
//         }

//         Cart cart = carts.get(0);
//         Optional<CartItem> existingItem = cart.getCartItems().stream()
//             .filter(item -> item.getProduct().equals(product))
//             .findFirst();

//         if (existingItem.isPresent()) {
//             CartItem itemToUpdate = existingItem.get();
//             itemToUpdate.setQuantity(itemToUpdate.getQuantity() + quantity);
//             itemToUpdate.setUpdatedAt(LocalDateTime.now());
//             cartItemRepository.save(itemToUpdate);
//         } else {
//             CartItem newItem = new CartItem(cart, product, quantity);
//             newItem.setCreatedAt(LocalDateTime.now());
//             cart.getCartItems().add(newItem);
//             cartItemRepository.save(newItem);
//         }
//     }

//     @Override
//     public void removeItemFromCart(Member member, Long productId) {
//         Cart cart = getCartForMember(member);
//         CartItem item = cartItemRepository.findByCartAndProduct_ProductId(cart, productId)
//                 .orElseThrow(() -> new RuntimeException("Item not found in cart"));

//         cartItemRepository.delete(item);
//     }
// }
