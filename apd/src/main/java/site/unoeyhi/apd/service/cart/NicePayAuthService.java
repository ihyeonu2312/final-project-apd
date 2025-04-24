package site.unoeyhi.apd.service.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class NicePayAuthService {

    @Value("${nicepay.api.auth-url}")
    private String authUrl;

    @Value("${nicepay.client.id}")
    private String clientId;

    @Value("${nicepay.client.secret}")
    private String clientSecret;


    private String accessToken;
    private long expireAt;

    private final RestTemplate restTemplate;

    public String getAccessToken() {
        try {
            long now = System.currentTimeMillis();
            if (accessToken != null && now < expireAt) {
                return accessToken;
            }
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // ✅ 중요!
            headers.setBasicAuth(clientId, clientSecret);        // ✅ Basic 인증
    
            Map<String, String> body = Map.of("grant_type", "client_credentials");
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
    
            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, request, Map.class);
    
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Object token = responseBody.get("access_token");
                if (token == null) token = responseBody.get("accessToken");
                if (token != null) {
                    accessToken = token.toString();
                    expireAt = System.currentTimeMillis() + (29 * 60 * 1000);
                    return accessToken;
                }
            }
            throw new RuntimeException("❌ AccessToken 발급 실패: 응답 없음");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ AccessToken 요청 중 예외 발생: " + e.getMessage());
        }
    }
    
}
