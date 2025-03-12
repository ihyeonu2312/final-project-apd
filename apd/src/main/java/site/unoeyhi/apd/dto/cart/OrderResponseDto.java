package site.unoeyhi.apd.dto.cart;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import site.unoeyhi.apd.entity.Order;

@Getter
@Setter
public class OrderResponseDto {
    private Long orderId;
    private LocalDateTime orderDate;
    private double totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String shippingStatus;
    private List<OrderItemDto> orderItems;

    public OrderResponseDto(Order order) {
        this.orderId = order.getOrderId();
        this.orderDate = order.getOrderDate();
        this.totalAmount = order.getTotalAmount();
        this.orderStatus = order.getOrderStatus().name();
        this.paymentStatus = order.getPaymentStatus().name();
        this.shippingStatus = order.getShippingStatus().name();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
    }
}
