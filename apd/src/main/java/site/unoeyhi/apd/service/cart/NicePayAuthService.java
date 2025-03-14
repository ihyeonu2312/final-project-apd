package site.unoeyhi.apd.service.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
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

    /**
     * ✅ Basic Auth를 이용해 Access Token 요청
     */
    public String getAccessToken() {
        long now = System.currentTimeMillis();
        if (accessToken != null && now < expireAt) {
            return accessToken; // 기존 Access Token 사용
        }

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedCredentials);

        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                authUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            accessToken = (String) response.getBody().get("accessToken");
            expireAt = System.currentTimeMillis() + (30 * 60 * 1000); // 30분 후 만료
            return accessToken;
        }

        throw new RuntimeException("Failed to retrieve access token");
    }
}
