package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.InicisPaymentRequestDto;
import site.unoeyhi.apd.service.cart.InicisPaymentService;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment/inicis")
@RequiredArgsConstructor
public class InicisPaymentController {

    private final InicisPaymentService inicisPaymentService;

    @PostMapping("/request")
    public InicisPaymentRequestDto initiatePayment(@RequestParam Long amount,
                                                    @RequestParam String orderId,
                                                    @RequestParam String buyerName) {
        return inicisPaymentService.createPaymentRequest(amount, orderId, buyerName);
    }

    @PostMapping("/result") //이니시스 Webhook 결제 성공여부
    public String paymentResult(@RequestParam Map<String, String> resultData) {
        String status = resultData.get("P_STATUS");  // 결제 성공 여부
        String rawOrderId = resultData.get("P_OID"); // 주문번호
        String amount = resultData.get("P_AMT");     // 결제 금액
        String message = resultData.get("P_RMESG1"); // 실패 메시지
    
        System.out.println("✅ 이니시스 결제 결과 수신:");
        System.out.println("- P_STATUS: " + status);
        System.out.println("- P_OID: " + rawOrderId);
        System.out.println("- P_AMT: " + amount);
        System.out.println("- P_RMESG1: " + message);
    
        if (status == null || rawOrderId == null) {
            System.out.println("❌ 결제 결과에 필수 정보 누락");
            return "FAIL"; // 이니시스에 실패 응답
        }
    
        // orderId 추출 (ORDER-12345 형태면 숫자만 가져오기)
        String orderId = rawOrderId.startsWith("ORDER-") ? rawOrderId.split("-")[1] : rawOrderId;
    
        if ("00".equals(status)) {
            // ✅ 결제 성공 처리
            try {
                // 여기서 주문 ID로 주문 상태 '결제완료'로 업데이트
                System.out.println("✅ 주문 완료 처리: orderId = " + orderId);
    
                // 예시) OrderService로 주문 완료 처리
                // orderService.completeOrder(Long.parseLong(orderId));
    
                return "OK"; // 이니시스에 성공 응답
            } catch (Exception e) {
                System.out.println("❌ 주문 완료 처리 중 오류 발생: " + e.getMessage());
                return "FAIL"; // 이니시스에 실패 응답
            }
        } else {
            // ❌ 결제 실패 또는 취소
            System.out.println("❌ 결제 실패 또는 취소: " + message);
            return "FAIL"; // 이니시스에 실패 응답
        }
    }
    
}
