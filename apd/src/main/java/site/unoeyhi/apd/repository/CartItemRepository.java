package site.unoeyhi.apd.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.unoeyhi.apd.entity.CartItem;



@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}