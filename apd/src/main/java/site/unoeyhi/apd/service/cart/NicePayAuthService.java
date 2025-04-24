package site.unoeyhi.apd.service.cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
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
                log.info("✅ 유효한 토큰이 캐시에 존재: {}", accessToken);
                return accessToken;
            }

            // ✅ Base64 Authorization 헤더 수동 확인용 로그
            String base64Auth = Base64.getEncoder()
                    .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
            log.info("🔐 Basic Auth Base64: {}", base64Auth);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(clientId, clientSecret); // ✅ 자동 Base64 인코딩

            Map<String, String> body = Map.of("grant_type", "client_credentials");
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            log.info("✅ 요청 URL: {}", authUrl);
            log.info("✅ Authorization 헤더: {}", headers.getFirst("Authorization"));
            log.info("✅ Content-Type: {}", headers.getContentType());
            log.info("✅ 요청 바디: {}", body);

            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, request, Map.class);

            log.info("✅ 응답 코드: {}", response.getStatusCode());
            log.info("✅ 응답 바디: {}", response.getBody());

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Object token = responseBody.get("access_token");
                if (token == null) token = responseBody.get("accessToken");
                if (token != null) {
                    accessToken = token.toString();
                    expireAt = System.currentTimeMillis() + (29 * 60 * 1000);
                    log.info("✅ AccessToken 발급 성공: {}", accessToken);
                    return accessToken;
                }
            }

            throw new RuntimeException("❌ AccessToken 발급 실패: 응답 없음 또는 access_token 없음");

        } catch (Exception e) {
            log.error("❌ AccessToken 요청 중 예외 발생", e);
            throw new RuntimeException("❌ AccessToken 요청 중 예외 발생: " + e.getMessage(), e);
        }
    }
}
