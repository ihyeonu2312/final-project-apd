package site.unoeyhi.apd.service.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Service
public class NicePayAuthService {

    private final RestTemplate restTemplate;

    @Value("${nicepay.api.auth-url}")
    private String authUrl;

    @Value("${nicepay.client.id}")
    private String clientId;

    @Value("${nicepay.client.secret}")
    private String clientSecret;

    private String accessToken;
    private long expireAt;

    public NicePayAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void checkProperties() {
        System.out.println("✅ [NicePayAuthService] 설정 확인");
        System.out.println("✅ clientId: " + clientId);
        System.out.println("✅ clientSecret: " + (clientSecret != null ? "✔️ 있음" : "❌ 없음"));
        System.out.println("✅ authUrl: " + authUrl);
    }

    public String getAccessToken() {
        try {
            System.out.println("🔐 [NicePay] AccessToken 요청 시작");
    
            long now = System.currentTimeMillis();
            if (accessToken != null && now < expireAt) {
                System.out.println("✅ [NicePay] 기존 accessToken 사용");
                return accessToken;
            }
    
            System.out.println("🔐 [NicePay] 기존 토큰 만료, 새로 발급 요청");
    
            // ✅ 본문 구성
            Map<String, String> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret
            );
            System.out.println("🧾 보낼 본문: " + body); // body 확인
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
    
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
    
            ResponseEntity<Map> response = restTemplate.exchange(
                authUrl, HttpMethod.POST, requestEntity, Map.class
            );
    
            System.out.println("🔐 응답 상태: " + response.getStatusCode());
            System.out.println("🔐 응답 바디: " + response.getBody());
    
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object tokenObj = response.getBody().get("accessToken");
    
                if (tokenObj != null) {
                    accessToken = tokenObj.toString();
                    expireAt = System.currentTimeMillis() + (29 * 60 * 1000);
                    System.out.println("✅ 발급된 accessToken: " + accessToken);
                    return accessToken;
                }
            }
    
            throw new RuntimeException("❌ AccessToken 발급 실패: 토큰 없음");
    
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ AccessToken 요청 중 예외 발생: " + e.getMessage());
        }
    }
    
}
