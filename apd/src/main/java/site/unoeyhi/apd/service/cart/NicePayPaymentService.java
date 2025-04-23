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
@RequiredArgsConstructor
@Service
public class NicePayAuthService {

    @Value("${nicepay.api.auth-url}")
    private String authUrl;

    @Value("${nicepay.client.id}")
    private String clientId;

    @Value("${nicepay.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    private String accessToken;
    private long expireAt;

    public String getAccessToken() {
        try {
            long now = System.currentTimeMillis();
            if (accessToken != null && now < expireAt) {
                return accessToken;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // ✅ 중요!
            headers.setBasicAuth(clientId, clientSecret); // ✅ Basic Auth

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials"); // ✅ key-value form으로 보냄

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.get("access_token") != null) {
                accessToken = responseBody.get("access_token").toString();
                expireAt = System.currentTimeMillis() + (29 * 60 * 1000);
                return accessToken;
            }

            throw new RuntimeException("❌ AccessToken 발급 실패: 응답 없음");
        } catch (Exception e) {
            throw new RuntimeException("❌ AccessToken 요청 중 예외 발생: " + e.getMessage());
        }
    }
}

