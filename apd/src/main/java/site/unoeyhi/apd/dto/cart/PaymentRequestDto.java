package site.unoeyhi.apd.dto.cart;

import lombok.Getter;
import lombok.Setter;
import site.unoeyhi.apd.eums.PaymentMethod;

@Getter
@Setter
public class PaymentRequestDto {
    private PaymentMethod paymentMethod;

    private String jwtToken;

    private double amount;

    // 기본 생성자
    public PaymentRequestDto() {}

    public PaymentRequestDto(PaymentMethod paymentMethod, double amount) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

}
