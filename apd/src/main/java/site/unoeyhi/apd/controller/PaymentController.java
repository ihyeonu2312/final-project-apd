package site.unoeyhi.apd.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.unoeyhi.apd.service.cart.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ✅ POST 방식 (이니시스 콜백)
    @PostMapping("/success")
    public ResponseEntity<String> paymentSuccess(HttpServletRequest request) {
        String authToken = request.getParameter("authToken");
        if (authToken == null || authToken.isEmpty()) {
            return ResponseEntity.badRequest().body("authToken 없음");
        }

        String result = paymentService.approve(authToken); // 승인 요청
        return ResponseEntity.ok(result);
    }

    // ✅ GET 방식 (테스트 또는 웹표준 방식)
    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccessGet(@RequestParam Map<String, String> params) {
        log.info("✅ [GET 결제 성공 콜백]");
        params.forEach((k, v) -> log.info("🔸 {} = {}", k, v));
        return ResponseEntity.ok("success (GET)");
    }
}
