package site.unoeyhi.apd.service.cart;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import site.unoeyhi.apd.dto.cart.PaymentInitiateResponseDto;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;
import site.unoeyhi.apd.entity.Order;
import site.unoeyhi.apd.entity.Payment;
import site.unoeyhi.apd.eums.PaymentStatus;
import site.unoeyhi.apd.repository.cart.OrderRepository;
import site.unoeyhi.apd.repository.cart.PaymentRepository;

@Log4j2
@Service
@RequiredArgsConstructor
public class NicePayPaymentService implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final NicePayAuthService authService;

    @Value("${nicepay.api.payment-url}")
    private String paymentApiUrl;

    @Value("${nicepay.return-url}")
    private String returnUrl;

    private final RestTemplate restTemplate; //RestTemplate 자동 주입
        

    @Override
    public PaymentInitiateResponseDto initiatePayment(Long orderId, PaymentRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        System.out.println("✅ [NicePay] initiatePayment 진입: orderId = " + orderId);


        // ✅ 액세스 토큰 가져오기
        System.out.println("✅ accessToken 요청 시작");
        String accessToken = authService.getAccessToken();
        System.out.println("✅ accessToken: " + accessToken);

        // ✅ 결제 요청 데이터 구성
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", order.getTotalAmount());
        payload.put("orderId", "ORDER-" + orderId);
        payload.put("goodsName", "장바구니 상품");
        payload.put("returnUrl", returnUrl);  // ex: https://unoeyhi.site/payment/success
        System.out.println("✅ 리턴 URL: " + returnUrl); //콜백

        payload.put("buyerName", "테스트고객");
        payload.put("buyerEmail", "test@example.com");
        payload.put("payMethod", requestDto.getPaymentMethod().name());

        // ✅ 요청 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(paymentApiUrl, entity, Map.class);

        System.out.println("✅ 응답 상태코드: " + response.getStatusCode());
        System.out.println("✅ 응답 바디: " + response.getBody());

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("nextRedirectUrl")) {
            log.error("❌ 결제 요청 실패: 응답 없음");
            throw new RuntimeException("결제 요청에 실패했습니다. 다시 시도해 주세요.");
        }

        String redirectUrl = (String) body.get("nextRedirectPcUrl");
        System.out.println("✅ 리다이렉트 URL: " + redirectUrl);


        // ✅ 결제 상태 저장 (옵션)
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(requestDto.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return new PaymentInitiateResponseDto(redirectUrl, "PENDING");
    }
}
