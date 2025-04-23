package site.unoeyhi.apd.service.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

@Service
public class NicePayAuthService {

    private final WebClient webClient;

    @Value("${nicepay.api.auth-url}")
    private String authUrl;

    @Value("${nicepay.client.id}")
    private String clientId;

    @Value("${nicepay.client.secret}")
    private String clientSecret;

    private String accessToken;
    private long expireAt;

    public NicePayAuthService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public String getAccessToken() {
        try {
            long now = System.currentTimeMillis();
            if (accessToken != null && now < expireAt) {
                return accessToken;
            }

            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            Map<String, String> tokenResponse = webClient.post()
                .uri(authUrl)
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();

        



            if (tokenResponse != null && tokenResponse.get("accessToken") != null) {
                accessToken = tokenResponse.get("accessToken").toString();
                expireAt = System.currentTimeMillis() + (29 * 60 * 1000);
                return accessToken;
            }

            throw new RuntimeException("❌ AccessToken 발급 실패: 응답 없음");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ AccessToken 요청 중 예외 발생: " + e.getMessage());
        }
    }
}
