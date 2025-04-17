package site.unoeyhi.apd.service.cart;

import site.unoeyhi.apd.dto.cart.PaymentInitiateResponseDto;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;
import site.unoeyhi.apd.dto.cart.PaymentResponseDto;

public interface PaymentService {
    PaymentInitiateResponseDto initiatePayment(Long orderId, PaymentRequestDto requestDto);
}
