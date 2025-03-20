package site.unoeyhi.apd.service.cart;

import java.util.List;

import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.eums.OrderStatus;

public interface OrderService {
    Order prepareOrder(Long memberId);  // 주문 임시 저장
    Order getOrder(Long orderId);
    List<Order> getOrdersByMember(Long memberId);  // ✅ 회원 주문 목록 조회 추가
    void updateOrderStatus(Long orderId, OrderStatus status);
    void completeOrder(Long orderId); //주문 완료
}