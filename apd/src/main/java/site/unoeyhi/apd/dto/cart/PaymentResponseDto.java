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
        this.paymentMethod = (payment.getPaymentMethod() != null) ? payment.getPaymentStatus().name() : null;
        this.paymentStatus = (payment.getPaymentStatus() != null) ? payment.getPaymentStatus().name() : null;
        this.amount = payment.getAmount();
        this.paymentDate = (payment.getPaymentDate() != null) ? payment.getPaymentDate() : LocalDateTime.now();

    }
}
