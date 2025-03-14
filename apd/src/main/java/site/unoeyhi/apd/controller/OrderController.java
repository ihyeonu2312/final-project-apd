package site.unoeyhi.apd.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.OrderResponseDto;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.service.cart.OrderService;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

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
}
