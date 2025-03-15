package site.unoeyhi.apd.repository.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Product;



@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteAllByCart(Cart cart);

    // 특정 장바구니의 모든 아이템 조회
    List<CartItem> findByCart(Cart cart);
}


