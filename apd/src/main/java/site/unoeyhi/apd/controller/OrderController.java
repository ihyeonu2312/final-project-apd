package site.unoeyhi.apd.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.OrderRequestDto;
import site.unoeyhi.apd.dto.cart.OrderResponseDto;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.eums.OrderStatus;
import site.unoeyhi.apd.service.cart.OrderService;

@RestController
@RequestMapping("/api/orders")  // ✅ RESTful URL
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** ✅ 결제 전 */
    @PostMapping("/prepare")
    public ResponseEntity<OrderResponseDto> prepareOrder(@RequestBody OrderRequestDto request) {
        System.out.println("🛒 주문 준비 요청 - memberId: " + request.getMemberId());

        Order order = orderService.prepareOrder(request.getMemberId());

        System.out.println("🛒 주문 준비 완료 - orderId: " + order.getOrderId());
        
        return ResponseEntity.ok(new OrderResponseDto(order));
    }
    //결제 후
    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long orderId) {
        System.out.println("💰 주문 완료 요청 - orderId: " + orderId);

        orderService.completeOrder(orderId); // ✅ 주문 완료 메서드 호출

        System.out.println("✅ 주문 완료 - orderId: " + orderId);
        
        return ResponseEntity.ok().build();
    }




    /** ✅ 특정 주문 조회 */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(new OrderResponseDto(order));
    }

    /** ✅ 회원의 모든 주문 조회 */
    @GetMapping("/list/{memberId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByMember(@PathVariable Long memberId) {
        List<Order> orders = orderService.getOrdersByMember(memberId);
        List<OrderResponseDto> orderDtos = orders.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDtos);
    }

    /** ✅ 주문 상태 변경 */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus status) {
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }
}
