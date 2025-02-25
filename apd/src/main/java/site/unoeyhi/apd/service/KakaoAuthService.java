package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.KakaoVerification;
import site.unoeyhi.apd.entity.Member.AuthType;
import site.unoeyhi.apd.repository.KakaoVerificationRepository;
import site.unoeyhi.apd.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {
    
    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final KakaoVerificationRepository kakaoVerificationRepository;

  public Map<String, String> getTokensFromKakao(String code) {
    String tokenUrl = "https://kauth.kakao.com/oauth/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    String body = "grant_type=authorization_code"
            + "&client_id=" + kakaoClientId
            + "&redirect_uri=" + kakaoRedirectUri
            + "&code=" + code;

    HttpEntity<String> request = new HttpEntity<>(body, headers);
    ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

    try {
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        String accessToken = jsonNode.get("access_token").asText();
        String refreshToken = jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null;

        // ✅ 두 개의 토큰을 Map으로 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        
        return tokens;
    } catch (Exception e) {
        log.error("🔥 카카오 액세스 토큰 파싱 오류", e);
        throw new RuntimeException("카카오 로그인 실패");
    }
}

    // ✅ 2️⃣ 액세스 토큰으로 카카오 사용자 정보 가져오기
    public Member getOrCreateKakaoUser(String accessToken, String refreshToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

        try {
            JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
            String email = jsonNode.get("kakao_account").get("email").asText();
            String nickname = jsonNode.get("properties").get("nickname").asText();

            // ✅ 기존 회원 조회
            Optional<Member> existingMember = memberRepository.findByEmail(email);
            Member member;

            if (existingMember.isPresent()) {
                member = existingMember.get();

                // ✅ 기존 회원이지만 카카오 로그인 방식이 아닐 경우 예외 발생
                if (member.getAuthType() != AuthType.KAKAO) {
                    throw new RuntimeException("해당 이메일은 다른 로그인 방식(EMAIL)으로 가입되어 있습니다. 카카오 로그인을 사용할 수 없습니다.");
                }
            } else {
                // ✅ 신규 회원 생성
                member = Member.builder()
                        .email(email)
                        .nickname(nickname)
                        .authType(AuthType.KAKAO) // ✅ 카카오 로그인 방식
                        .password(null)  // ✅ 카카오는 비밀번호 없음
                        .role(Member.Role.일반회원)  // ✅ 기본 회원 권한 설정
                        .status(Member.MemberStatus.ACTIVE)  // ✅ 카카오 로그인은 활성 상태로 처리
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                memberRepository.save(member);
            }

            // ✅ `kakao_verification` 테이블에서 기존 인증 정보 확인
            Optional<KakaoVerification> kakaoVerificationOpt = kakaoVerificationRepository.findByMember_MemberId(member.getMemberId());


            if (kakaoVerificationOpt.isPresent()) {
                // ✅ 기존 데이터가 있다면 업데이트
                KakaoVerification kakaoVerification = kakaoVerificationOpt.get();
                kakaoVerification.setKakaoAccessToken(accessToken);
                kakaoVerification.setKakaoRefreshToken(refreshToken);
                kakaoVerification.setStatus("ACTIVE");
                kakaoVerificationRepository.save(kakaoVerification);
            } else {
                // ✅ 새로운 카카오 인증 정보 저장
                KakaoVerification kakaoVerification = new KakaoVerification(member, accessToken, refreshToken, "ACTIVE");
                kakaoVerificationRepository.save(kakaoVerification);
            }

            return member;

        } catch (Exception e) {
            log.error("🔥 카카오 사용자 정보 파싱 오류", e);
            throw new RuntimeException("카카오 로그인 실패");
        }
    }
}
