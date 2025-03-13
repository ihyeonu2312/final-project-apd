package site.unoeyhi.apd.service.cart;

import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.entity.Payment;
import site.unoeyhi.apd.eums.OrderStatus;
import site.unoeyhi.apd.eums.PaymentMethod;

public interface OrderService {
    Order createOrder(Long memberId);
    Order getOrder(Long orderId);
    Payment processPayment(Long orderId, PaymentMethod paymentMethod);
    //주문 상태 업데이트 메서드
    void updateOrderStatus(Long orderId, OrderStatus status);
}
