package site.unoeyhi.apd.controller;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;
import site.unoeyhi.apd.dto.cart.PaymentResponseDto;
import site.unoeyhi.apd.service.cart.PaymentService;

@Log4j2
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
    @PostMapping("/result")
    public ResponseEntity<String> handleWebhook(@RequestParam Map<String, String> data) {
    log.info("💳 나이스페이 웹훅 수신: {}", data);

    String resultCode = data.get("resultCode");
    String moid = data.get("moid"); // ex: ORDER-12345
    String tid = data.get("tid");
    String amount = data.get("amount");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_PLAIN);
    
        if ("3001".equals(resultCode)) {
            // ✅ 결제 성공 처리
            // 주문 ID 파싱해서 주문 상태 업데이트 로직 추가
        } else {
            // ❌ 결제 실패 처리
        }

        return ResponseEntity.ok("OK");
    }


}
