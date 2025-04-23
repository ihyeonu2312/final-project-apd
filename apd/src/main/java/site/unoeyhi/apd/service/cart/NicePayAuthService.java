package site.unoeyhi.apd.service.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret);

            // 요청 바디 설정
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // 요청 전송
            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, request, Map.class);
            
            System.out.println("✅ 요청 URL: " + authUrl);
            System.out.println("✅ Content-Type: " + headers.getContentType());
            System.out.println("✅ BasicAuth: " + headers.getFirst("Authorization"));
            System.out.println("✅ Body: " + body);


            // 응답 파싱
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                System.out.println("✅ 응답 내용: " + responseBody); // 👈 키 확인
            
                Object token = responseBody.get("access_token");
                if (token == null) token = responseBody.get("accessToken"); // ✅ 대소문자 둘 다 시도하여 확인
            
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
