package site.unoeyhi.apd.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.entity.Payment;
import site.unoeyhi.apd.eums.PaymentMethod;
import site.unoeyhi.apd.service.cart.OrderService;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Long> request) {
        Long memberId = request.get("memberId");
        Order order = orderService.createOrder(memberId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Payment> processPayment(@PathVariable Long orderId, @RequestBody Map<String, String> request) {
        PaymentMethod paymentMethod = PaymentMethod.valueOf(request.get("paymentMethod"));
        Payment payment = orderService.processPayment(orderId, paymentMethod);
        return ResponseEntity.ok(payment);
    }
}

