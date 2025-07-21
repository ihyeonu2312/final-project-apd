package site.unoeyhi.apd.dto.cart;

import lombok.Getter;
import lombok.Setter;
import site.unoeyhi.apd.eums.PaymentMethod;

@Getter
@Setter
public class PaymentRequestDto {
    private PaymentMethod paymentMethod;

    private String jwtToken;

    private Long amount;

    private String buyerName;

    // 기본 생성자
    public PaymentRequestDto() {}

    public PaymentRequestDto(PaymentMethod paymentMethod, Long amount, String buyerName) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.buyerName = buyerName;
    }

}
