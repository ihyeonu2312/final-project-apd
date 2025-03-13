package site.unoeyhi.apd.service.cart;

import site.unoeyhi.apd.dto.cart.PaymentRequestDto;
import site.unoeyhi.apd.dto.cart.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto processPayment(Long orderId, PaymentRequestDto requestDto);
}
