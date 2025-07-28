package site.unoeyhi.apd.dto.cart;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentInitiateResponseDto {
    private String redirectUrl;                
    private InicisPaymentRequestDto requestData;  // form 으로 넘길 데이터
}
 