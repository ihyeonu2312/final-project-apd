package site.unoeyhi.apd.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @PostMapping("/success")
    public ResponseEntity<String> paymentSuccess(HttpServletRequest request) {
        log.info("✅ NICEPAY 결제 완료 콜백 수신");

        // 폼 파라미터 로그 찍기
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String value = request.getParameter(name);
            log.info("🔸 {} = {}", name, value);
        }

        // TODO: 여기에 orderId, 결제 상태 파싱 → 주문 완료 처리 로직 연결
        // ex) orderService.completeOrder(orderId);

        return ResponseEntity.ok("success");
    }
}
