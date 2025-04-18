package site.unoeyhi.apd.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import site.unoeyhi.apd.dto.cart.PaymentInitiateResponseDto;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;
import site.unoeyhi.apd.dto.cart.PaymentResponseDto;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.entity.Payment;
import site.unoeyhi.apd.eums.OrderStatus;
import site.unoeyhi.apd.eums.PaymentStatus;
import site.unoeyhi.apd.repository.cart.OrderRepository;
import site.unoeyhi.apd.repository.cart.PaymentRepository;
import site.unoeyhi.apd.service.cart.PaymentService;

@Log4j2
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 주문 결제 요청 (NicePay 연동)
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> initiatePayment(
        @PathVariable Long orderId,
        @RequestBody PaymentRequestDto requestDto) {
            log.info("✅ [PaymentController] /{}/pay 요청 수신", orderId);
        try {
            // ✅ `NicePayPaymentService`에서 처리하도록 위임 (Access Token 포함)
            PaymentInitiateResponseDto responseDto = paymentService.initiatePayment(orderId, requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 요청 실패: " + e.getMessage());
        }
    }

    @PostMapping("/result")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("💳 [NICEPAY 웹훅 수신]: {}", payload);

        String resultCode = (String) payload.get("resultCode"); // 3001: 결제 성공
        String moid = (String) payload.get("moid"); // ex: ORDER-3
        String tid = (String) payload.get("tid");
        String amountStr = (String) payload.get("amount");

        try {
            if (!"3001".equals(resultCode)) {
                log.warn("❌ 결제 실패 또는 취소: resultCode = {}", resultCode);
                return ResponseEntity.badRequest().body("결제 실패: " + resultCode); // 실패 코드를 명시적으로 반환
        }
            
            // 주문 ID 파싱
            Long orderId = Long.parseLong(moid.replace("ORDER-", ""));

            // ✅ 주문 조회
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

            // ✅ 결제 상태 업데이트
            Payment payment = paymentRepository.findByOrder(order)
                    .orElseThrow(() -> new RuntimeException("결제 정보 없음"));

            double paidAmount = Double.parseDouble(amountStr);
            if (paidAmount != order.getTotalAmount()) {
                log.warn("❌ 결제 금액 불일치");
                return ResponseEntity.badRequest().body("Invalid amount");
            }

            payment.setPaymentStatus(PaymentStatus.PAID); // 또는 SUCCESS 등 enum 값에 따라
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);

            // ✅ 주문 상태도 변경
            order.setOrderStatus(OrderStatus.PAID);
            orderRepository.save(order);



            log.info("✅ 결제 성공 처리 완료 (주문 ID: {})", orderId);

            
            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("❌ 웹훅 처리 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }
    }



}
