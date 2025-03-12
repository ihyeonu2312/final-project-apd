package site.unoeyhi.apd.service.cart;

import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.entity.Payment;
import site.unoeyhi.apd.eums.PaymentMethod;

public interface OrderService {
    Order createOrder(Long memberId);
    Order getOrder(Long orderId);
    Payment processPayment(Long orderId, PaymentMethod paymentMethod);
}
