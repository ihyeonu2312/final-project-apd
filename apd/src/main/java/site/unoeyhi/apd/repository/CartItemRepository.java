package site.unoeyhi.apd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;



@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart); // Cart에 속한 모든 CartItem을 반환
    Optional<CartItem> findByCartAndProduct_ProductId(Cart cart, Long productId);
}