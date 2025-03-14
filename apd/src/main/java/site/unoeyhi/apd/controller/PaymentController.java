package site.unoeyhi.apd.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;
import site.unoeyhi.apd.dto.cart.PaymentResponseDto;
import site.unoeyhi.apd.service.cart.PaymentService;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 주문 결제 요청 (NicePay 연동)
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> processPayment(
        @PathVariable Long orderId,
        @RequestBody PaymentRequestDto requestDto) {
        
        try {
            // ✅ `NicePayPaymentService`에서 처리하도록 위임 (Access Token 포함)
            PaymentResponseDto responseDto = paymentService.processPayment(orderId, requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 요청 실패: " + e.getMessage());
        }
    }
}
