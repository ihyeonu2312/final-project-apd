package site.unoeyhi.apd.dto.cart;

import lombok.Getter;
import lombok.Setter;
import site.unoeyhi.apd.entity.Payment;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponseDto {
    private Long paymentId;
    private Long orderId;
    private String paymentMethod;
    private String paymentStatus;
    private double amount;
    private LocalDateTime paymentDate;

    public PaymentResponseDto(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.orderId = payment.getOrder().getOrderId();
        this.paymentMethod = payment.getPaymentMethod().name();
        this.paymentStatus = payment.getPaymentStatus().name();
        this.amount = payment.getAmount();
        this.paymentDate = payment.getPaymentDate();
    }
}
