package site.unoeyhi.apd.repository.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.unoeyhi.apd.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
