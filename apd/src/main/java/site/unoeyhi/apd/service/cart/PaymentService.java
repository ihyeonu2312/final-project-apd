package site.unoeyhi.apd.service.cart;

import site.unoeyhi.apd.dto.cart.PaymentResponseDto;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;

public interface PaymentService {
    PaymentResponseDto initiatePayment(Long orderId, PaymentRequestDto requestDto);
}
