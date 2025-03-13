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
import java.util.Base64;

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

@Service
@RequiredArgsConstructor
public class NicePayPaymentService implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Value("${nicepay.api.url}")
    private String nicePayApiUrl;

    @Value("${nicepay.client.id}")
    private String clientId;

    @Value("${nicepay.client.secret}")
    private String clientSecret;

    @Override
    public PaymentResponseDto processPayment(Long orderId, PaymentRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // 1️⃣ NICE API에 보낼 결제 요청 데이터 생성
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("clientId", clientId);
        paymentRequest.put("orderId", order.getOrderId());
        paymentRequest.put("amount", order.getTotalAmount());
        paymentRequest.put("paymentMethod", requestDto.getPaymentMethod().name());
        paymentRequest.put("returnUrl", "http://localhost:8080/order/payment/complete");

        // 2️⃣ NICE API로 결제 요청 보내기
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(paymentRequest, headers);

        ResponseEntity<PaymentResponseDto> response = restTemplate.exchange(
            nicePayApiUrl,
            HttpMethod.POST,
            requestEntity,
            PaymentResponseDto.class
        );


        // 3️⃣ 응답을 기반으로 결제 상태 저장
        PaymentResponseDto paymentResponse = response.getBody();
        if (paymentResponse == null || !"SUCCESS".equals(paymentResponse.getPaymentStatus())) {
            throw new RuntimeException("Payment failed");
        }

        // 4️⃣ 결제 정보 DB 저장
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.valueOf(paymentResponse.getPaymentMethod()));
        payment.setAmount(paymentResponse.getAmount());
        payment.setPaymentStatus(PaymentStatus.valueOf(paymentResponse.getPaymentStatus()));
        payment.setPaymentDate(paymentResponse.getPaymentDate());
        paymentRepository.save(payment);

        // 5️⃣ 주문 상태 업데이트
        order.setOrderStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        return paymentResponse;
    }
}
