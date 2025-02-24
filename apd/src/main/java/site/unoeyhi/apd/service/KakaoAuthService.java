package site.unoeyhi.apd.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.Member.AuthType;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.util.JwtUtil;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @Value("${KAKAO_CLIENT_ID}")
    private String kakaoClientId;

    @Value("${KAKAO_CLIENT_SECRET}")
    private String kakaoClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    // ✅ 1. 카카오 로그인 URL 생성
    public String getKakaoLoginUrl() {
        return UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("client_id", kakaoClientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .toUriString();
    }

    // ✅ 2. 카카오 인가 코드로 액세스 토큰 요청
    public String kakaoLogin(String code) {
        String accessToken = getAccessToken(code);
        JsonNode userInfo = getUserInfo(accessToken);

        String kakaoEmail = userInfo.get("kakao_account").get("email").asText();
        String nickname = userInfo.get("properties").get("nickname").asText();

        // ✅ 3. 이메일로 기존 회원 조회
        Optional<Member> existingMember = memberRepository.findByEmail(kakaoEmail);

        if (existingMember.isPresent()) {
            // 기존 회원이면 JWT 토큰 반환
            return jwtUtil.generateToken(existingMember.get().getEmail());
        } else {
            // 새로운 회원 가입
            Member newMember = Member.builder()
                    .email(kakaoEmail)
                    .nickname(nickname)
                    .authType(AuthType.KAKAO) // ✅ 카카오 회원
                    .build();

            memberRepository.save(newMember);
            return jwtUtil.generateToken(newMember.getEmail());
        }
    }

    // ✅ 4. 카카오 액세스 토큰 요청
    private String getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", kakaoClientId)
                .queryParam("client_secret", kakaoClientSecret)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                .build().toUriString().substring(1);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

        try {
            JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("카카오 액세스 토큰 요청 실패", e);
        }
    }

    // ✅ 5. 카카오 유저 정보 가져오기
    private JsonNode getUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

        try {
            return new ObjectMapper().readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("카카오 유저 정보 요청 실패", e);
        }
    }
}
