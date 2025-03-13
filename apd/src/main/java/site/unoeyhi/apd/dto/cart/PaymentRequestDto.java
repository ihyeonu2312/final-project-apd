package site.unoeyhi.apd.dto.cart;

import lombok.Getter;
import lombok.Setter;
import site.unoeyhi.apd.eums.PaymentMethod;

@Getter
@Setter
public class PaymentRequestDto {
    private PaymentMethod paymentMethod;
}
