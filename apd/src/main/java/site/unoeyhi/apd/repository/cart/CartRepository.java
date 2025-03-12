package site.unoeyhi.apd.repository.cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.Member;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMember(Member member);
}
