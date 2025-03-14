package site.unoeyhi.apd.service.cart;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;
import site.unoeyhi.apd.dto.cart.PaymentResponseDto;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.entity.Payment;
import site.unoeyhi.apd.eums.OrderStatus;
import site.unoeyhi.apd.eums.PaymentMethod;
import site.unoeyhi.apd.eums.PaymentStatus;
import site.unoeyhi.apd.repository.cart.OrderRepository;
import site.unoeyhi.apd.repository.cart.PaymentRepository;
import site.unoeyhi.apd.service.cart.NicePayAuthService;

@Service
@RequiredArgsConstructor
public class NicePayPaymentService implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final NicePayAuthService authService;

    @Value("${nicepay.api.auth-url}")
    private String nicePayApiUrl;

    @Value("${nicepay.client.id:}")
    private String clientId;

    @Value("${nicepay.client.secret:}")
    private String clientSecret;

    @PostConstruct
    public void checkConfig() {
        System.out.println("✅ NicePay API URL: " + nicePayApiUrl);
        System.out.println("✅ NicePay Client ID: " + clientId);
        System.out.println("✅ NicePay Client Secret: " + (clientSecret.isEmpty() ? "❌ 설정 안됨" : "✅ 설정됨"));
    }
    
    @Override
    public PaymentResponseDto processPayment(Long orderId, PaymentRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // ✅ Access Token 가져오기
        String accessToken = authService.getAccessToken();

        // ✅ NICE API에 보낼 결제 요청 데이터 생성
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", order.getOrderId());
        paymentRequest.put("amount", order.getTotalAmount());
        paymentRequest.put("paymentMethod", requestDto.getPaymentMethod().name());
        paymentRequest.put("returnUrl", "http://localhost:8080/order/payment/complete");

        // ✅ Access Token을 사용한 인증 방식 적용
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(paymentRequest, headers);

        ResponseEntity<PaymentResponseDto> response = restTemplate.exchange(
            nicePayApiUrl,
            HttpMethod.POST,
            requestEntity,
            PaymentResponseDto.class
        );

        // ✅ 응답을 기반으로 결제 상태 저장
        PaymentResponseDto paymentResponse = response.getBody();
        if (paymentResponse == null || !"SUCCESS".equals(paymentResponse.getPaymentStatus())) {
            throw new RuntimeException("Payment failed");
        }

        // ✅ 결제 정보 DB 저장
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.valueOf(paymentResponse.getPaymentMethod()));
        payment.setAmount(paymentResponse.getAmount());
        payment.setPaymentStatus(PaymentStatus.valueOf(paymentResponse.getPaymentStatus()));
        payment.setPaymentDate(paymentResponse.getPaymentDate());
        paymentRepository.save(payment);

        // ✅ 주문 상태 업데이트
        order.setOrderStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        return paymentResponse;
    }
}
