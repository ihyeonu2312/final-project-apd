package site.unoeyhi.apd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.Member;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    //Optional<Cart> findByMember(Member member); 단일 개체 사용시 사용  // 특정 회원의 장바구니 조회
    List<Cart> findByMember(Member member);
    
}
