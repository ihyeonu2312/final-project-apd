package site.unoeyhi.apd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.unoeyhi.apd.entity.*;
import site.unoeyhi.apd.entity.dto.CartItemDto;
import site.unoeyhi.apd.repository.*;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartItem addProductToCart(CartItemDto cartItemDto) {
        // 1. 장바구니 확인 (없으면 생성)
        Cart cart = cartRepository.findById(cartItemDto.getCartId())
                .orElseThrow(() -> new IllegalArgumentException("장바구니 ID가 존재하지 않습니다."));

        // 2. 상품 확인
        Product product = productRepository.findById(cartItemDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품 ID가 존재하지 않습니다."));

        // 3. 장바구니 항목 추가
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(cartItemDto.getQuantity())
                .build();

        return cartItemRepository.save(cartItem);
    }
}
