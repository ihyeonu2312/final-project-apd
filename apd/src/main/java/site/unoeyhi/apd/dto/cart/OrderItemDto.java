package site.unoeyhi.apd.dto.cart;

import lombok.Getter;
import lombok.Setter;
import site.unoeyhi.apd.entity.OrderItem;

@Getter
@Setter
public class OrderItemDto {
    private Long productId;
    private int quantity;
    private double price;

    public OrderItemDto(OrderItem orderItem) {
        this.productId = orderItem.getProduct().getProductId();
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getPrice();
    }
}
