package site.unoeyhi.apd.service.cart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.Cart;
import site.unoeyhi.apd.entity.CartItem;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.entity.OrderItem;
import site.unoeyhi.apd.entity.Payment;
import site.unoeyhi.apd.eums.OrderStatus;
import site.unoeyhi.apd.eums.PaymentMethod;
import site.unoeyhi.apd.eums.PaymentStatus;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.repository.cart.CartItemRepository;
import site.unoeyhi.apd.repository.cart.CartRepository;
import site.unoeyhi.apd.repository.cart.OrderItemRepository;
import site.unoeyhi.apd.repository.cart.OrderRepository;
import site.unoeyhi.apd.repository.cart.PaymentRepository;
import site.unoeyhi.apd.repository.product.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Override
    public Order createOrder(Long memberId) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 2. 장바구니 조회
        Cart cart = cartRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 비어 있습니다."));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니에 상품이 없습니다.");
        }

        // 3. 주문 생성
        Order order = new Order();
        order.setMember(member);
        order.setOrderStatus(OrderStatus.PROCESSING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        // 4. 주문 아이템 추가 & 총 금액 계산
        double totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            totalAmount += cartItem.getQuantity() * cartItem.getProduct().getPrice();
            orderItems.add(orderItem);
        }
        order.setTotalAmount(totalAmount);

        // 5. 주문 저장
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // 6. 장바구니 비우기
        cartItemRepository.deleteAll(cartItems);
        cartRepository.delete(cart);

        return order;
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));
    }

    @Override
    public Payment processPayment(Long orderId, PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("이미 결제 완료된 주문입니다.");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());

        paymentRepository.save(payment);

        // 주문 결제 상태 업데이트
        order.setPaymentStatus(PaymentStatus.PAID);

        orderRepository.save(order); // ✅ 변경된 주문 저장

        return payment;

    }
}