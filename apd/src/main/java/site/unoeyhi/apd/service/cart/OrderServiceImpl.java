package site.unoeyhi.apd.service.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.eums.OrderStatus;
import site.unoeyhi.apd.repository.cart.OrderRepository;
import site.unoeyhi.apd.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    @Override
    public Order createOrder(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Order order = new Order();
        order.setMember(member);
        order.setOrderStatus(OrderStatus.PROCESSING);
        order.setOrderDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));
    }

    @Override
    public List<Order> getOrdersByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        return orderRepository.findByMember(member);
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));
        order.setOrderStatus(status);
        orderRepository.save(order);
    }
}
