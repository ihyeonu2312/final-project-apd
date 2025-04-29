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

    @PostMapping("/result")
    public String paymentResult(@RequestParam Map<String, String> resultData) {
        // 이니시스에서 POST로 넘겨주는 결제 결과를 처리
        System.out.println("결제 결과 수신: " + resultData);
        return "OK";
    }
}
