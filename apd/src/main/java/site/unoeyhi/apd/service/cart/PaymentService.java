package site.unoeyhi.apd.service.cart;

import site.unoeyhi.apd.dto.cart.PaymentResponseDto;
import site.unoeyhi.apd.dto.cart.PaymentInitiateResponseDto;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;

public interface PaymentService {
    PaymentInitiateResponseDto initiatePayment(Long orderId, PaymentRequestDto requestDto);
    String approve(String  authToken);
    
    
    
}
