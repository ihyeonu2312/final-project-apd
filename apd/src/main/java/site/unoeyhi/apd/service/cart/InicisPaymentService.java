package site.unoeyhi.apd.service.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.dto.cart.InicisPaymentRequestDto;
import site.unoeyhi.apd.dto.cart.PaymentInitiateResponseDto;
import site.unoeyhi.apd.dto.cart.PaymentRequestDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InicisPaymentService implements PaymentService  {

    @Value("${spring.inicis.mid}")
    private String mid;

    @Value("${spring.inicis.signKey}")
    private String signKey;

    @Value("${spring.inicis.apiUrl}")
    private String apiUrl;

    public InicisPaymentRequestDto createPaymentRequest(Long amount, String orderId, String buyerName) {
        String timestamp = getCurrentTimestamp();
        String hashData = generateHashData(mid, orderId, amount, timestamp, signKey);

        return InicisPaymentRequestDto.builder()
                .mid(mid)
                .orderId(orderId)
                .price(amount.toString())
                .buyerName(buyerName)
                .timestamp(timestamp)
                .hashData(hashData)
                .apiUrl(apiUrl)
                .returnUrl("https://unoeyhi.site/payment-redirect.html")
                .build();
    }

    private String generateHashData(String mid, String orderId, Long amount, String timestamp, String signKey) {
        try {
            String data = mid + orderId + amount + timestamp + signKey;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
 
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public String approvePayment(String authToken) {
        String type = "approve";
        String timestamp = getCurrentTimestamp();

        // signature = SHA-256(authToken + timestamp + signKey)
        String signature = generateSignature(authToken, timestamp, signKey);

        // 요청 body 구성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("type", type);
        body.add("authToken", authToken);
        body.add("mid", mid);
        body.add("signature", signature);
        body.add("timestamp", timestamp);
        body.add("charset", "UTF-8");
        body.add("format", "JSON");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
        return response.getBody();  // JSON 응답 (성공/실패 결과)
    }
    private String generateSignature(String authToken, String timestamp, String signKey) {
        try {
            String data = authToken + timestamp + signKey;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Signature hashing error", e);
        }
    }

     @Override
    public String approve(String authToken) {
        return "✅ 결제 승인 완료 (authToken: " + authToken + ")";
    }

    @Override
    public PaymentInitiateResponseDto initiatePayment(Long orderId, PaymentRequestDto requestDto) {
        String orderIdStr = "ORDER-" + orderId;
        String buyerName = requestDto.getBuyerName();
        Long amount = requestDto.getAmount();

        InicisPaymentRequestDto inicisRequest = createPaymentRequest(amount, orderIdStr, buyerName);

        return PaymentInitiateResponseDto.builder()
                .redirectUrl(inicisRequest.getApiUrl())  // ex) https://mobile.inicis.com/smart/payment/
                .requestData(inicisRequest)              // 이건 프론트에서 form으로 submit할 때 사용될 수 있음
                .build();
    }

}
