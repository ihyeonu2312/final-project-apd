package site.unoeyhi.apd.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.OrderResponseDto;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;
import site.unoeyhi.apd.dto.cart.PaymentResponseDto;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.eums.OrderStatus;
import site.unoeyhi.apd.service.cart.OrderService;
import site.unoeyhi.apd.service.cart.PaymentService;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService; 

    /**
     * 새로운 주문 생성
     */
    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Long> request) {
        Long memberId = request.get("memberId");
        Order order = orderService.createOrder(memberId);
        return ResponseEntity.ok(order);
    }

    /**
     * 주문 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(new OrderResponseDto(order));
    }

    /**
     * 주문 결제 요청 (나이스페이 연동)
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<PaymentResponseDto> processPayment(
        @PathVariable Long orderId,
        @RequestBody PaymentRequestDto requestDto) {

        PaymentResponseDto paymentResponse = paymentService.processPayment(orderId, requestDto);
        return ResponseEntity.ok(paymentResponse); // ✅ PaymentResponseDto 반환
    }

    /**
     * 나이스페이 Webhook (결제 완료 처리)
     */
    @PostMapping("/payment/complete")
    public ResponseEntity<?> completePayment(@RequestBody PaymentResponseDto response) {
        if ("SUCCESS".equals(response.getPaymentStatus())) {
            // ✅ 주문 상태를 "PROCESSING"으로 업데이트
            orderService.updateOrderStatus(response.getOrderId(), OrderStatus.PROCESSING);
            return ResponseEntity.ok("결제 성공!");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 실패");
    }
}
