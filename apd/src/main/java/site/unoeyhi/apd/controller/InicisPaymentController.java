package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.PaymentInitiateResponseDto;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;
import site.unoeyhi.apd.service.cart.InicisPaymentService;
import site.unoeyhi.apd.service.cart.OrderService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment/inicis")
@RequiredArgsConstructor
public class InicisPaymentController {

    private final OrderService orderService;
    private final InicisPaymentService inicisPaymentService;

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<PaymentInitiateResponseDto> initiatePayment(
            @PathVariable("orderId") Long orderId,
            @RequestBody PaymentRequestDto requestDto) {

        PaymentInitiateResponseDto response = inicisPaymentService.initiatePayment(orderId, requestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/result")
    public String paymentResult(@RequestParam Map<String, String> resultData) {
        String status = resultData.get("P_STATUS");
        String rawOrderId = resultData.get("P_OID");
        String amount = resultData.get("P_AMT");
        String message = resultData.get("P_RMESG1");

        System.out.println("✅ [이니시스 Webhook] 결제 결과 수신");
        System.out.println("- P_STATUS: " + status);
        System.out.println("- P_OID: " + rawOrderId);
        System.out.println("- P_AMT: " + amount);
        System.out.println("- P_RMESG1: " + message);

        if (status == null || rawOrderId == null) {
            System.out.println("❌ 필수 정보 누락");
            return "FAIL";
        }

        String orderId = rawOrderId.startsWith("ORDER-") ? rawOrderId.split("-")[1] : rawOrderId;

        if ("00".equals(status)) {
            try {
                System.out.println("✅ 주문 완료 처리 시도: orderId = " + orderId);
                orderService.completeOrder(Long.parseLong(orderId));  // 실제 상태 변경

                return "OK"; // ✅ 이니시스에 성공 응답
            } catch (Exception e) {
                System.out.println("❌ 주문 처리 실패: " + e.getMessage());
                return "FAIL";
            }
        } else {
            System.out.println("❌ 결제 실패 또는 취소: " + message);
            return "FAIL";
        }
    }
}
