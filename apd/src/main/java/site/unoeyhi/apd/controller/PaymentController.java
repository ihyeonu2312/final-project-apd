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

    @PostMapping("/payment/success")
    public ResponseEntity<String> handlePaymentSuccess(HttpServletRequest request) {
        System.out.println("✅ [결제 완료 콜백 수신]");

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String value = request.getParameter(name);
            System.out.println("🔸 " + name + " = " + value);
        }

        return ResponseEntity.ok("success");
    }
}
