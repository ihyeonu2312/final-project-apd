package site.unoeyhi.apd.service.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.entity.OrderItem;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.eums.OrderStatus;
import site.unoeyhi.apd.eums.PaymentStatus;
import site.unoeyhi.apd.eums.ShippingStatus;
import site.unoeyhi.apd.repository.cart.CartItemRepository;
import site.unoeyhi.apd.repository.cart.CartRepository;
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
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    @Override
    public Order createOrder(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Cart cart = cartRepository.findByMember(member)
            .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다.");
        }
        // ✅ 주문 객체 생성 (아직 저장 X)
        Order order = new Order();
        order.setMember(member);
        order.setOrderStatus(OrderStatus.PROCESSING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setShippingStatus(ShippingStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        // ✅ 장바구니 상품을 `OrderItem`으로 변환 후 `Order`에 추가
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> new OrderItem(order, cartItem.getProduct(), cartItem.getQuantity(), cartItem.getPrice()))
                .toList();

        order.setOrderItems(orderItems); // ✅ Order에 추가
        order.calculateTotalAmount(); // ✅ 총 금액 계산
        orderRepository.save(order); // ✅ Order와 OrderItem 함께 저장

        System.out.println("✅ 주문 생성 완료 - 총 금액: " + order.getTotalAmount());
        
        return order;
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
