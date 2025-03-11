package site.unoeyhi.apd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.KakaoVerification;
import site.unoeyhi.apd.entity.Member.AuthType;
import site.unoeyhi.apd.repository.KakaoVerificationRepository;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.util.JwtUtil;

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

      private final JwtUtil jwtUtil; 

public Map<String, String> getTokensFromKakao(String code) {
    String tokenUrl = "https://kauth.kakao.com/oauth/token";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    String body = "grant_type=authorization_code"
            + "&client_id=" + kakaoClientId
            + "&redirect_uri=" + kakaoRedirectUri
            + "&code=" + code;

    HttpEntity<String> request = new HttpEntity<>(body, headers);
    
    try {
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);
        
        log.info("✅ 카카오 토큰 응답: " + response.getBody());  // 🔍 응답 로그 추가

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        String accessToken = jsonNode.get("access_token").asText();
        String refreshToken = jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null;

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);

        return tokens;
    } catch (HttpClientErrorException e) {
        log.error("🔥 카카오 토큰 요청 실패: " + e.getResponseBodyAsString());
        throw new RuntimeException("카카오 로그인 실패");
    } catch (Exception e) {
        log.error("🔥 카카오 액세스 토큰 파싱 오류", e);
        throw new RuntimeException("카카오 로그인 실패");
    }
}
public Map<String, String> kakaoLogin(String code) {
    Map<String, String> kakaoTokens = getTokensFromKakao(code);
    String accessToken = kakaoTokens.get("access_token");
    String refreshToken = kakaoTokens.get("refresh_token");

    Member member = getOrCreateKakaoUser(accessToken, refreshToken);

    // ✅ JWT 발급
    String jwt = jwtUtil.generateTokenForKakao(member.getKakaoId());

    Map<String, String> result = new HashMap<>();
    result.put("jwt", jwt);
    result.put("access_token", accessToken);
    result.put("refresh_token", refreshToken);
    
    return result;
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
    
            // ✅ 응답 데이터 콘솔 출력
            System.out.println("🔥 카카오 사용자 정보 응답: " + jsonNode.toPrettyString());
    
            Long kakaoId = jsonNode.get("id").asLong();  // ✅ 카카오 사용자 고유 ID
            String email = null;
            String connectedAt = jsonNode.get("connected_at").asText(); // ✅ 가입 시간 (ISO 8601)
            
            LocalDateTime createdAt = LocalDateTime.parse(connectedAt.substring(0, 19)); // UTC 변환
    
            // ✅ 기존 회원 조회 (카카오 ID 기준)
            Optional<Member> existingMember = memberRepository.findByKakaoId(kakaoId);
            Member member;
    
            if (existingMember.isPresent()) {
                member = existingMember.get();
                
    
                // ✅ 기존 회원이지만 카카오 로그인 방식이 아닐 경우 예외 발생
                if (member.getAuthType() != AuthType.KAKAO) {
                    throw new RuntimeException("해당 계정은 EMAIL 방식으로 가입되어 있습니다. 카카오 로그인을 사용할 수 없습니다.");
                }
            } else {
                // ✅ 신규 회원 생성 (닉네임을 카카오 ID로 설정)
                member = Member.builder()
                        .kakaoId(kakaoId) // ✅ 카카오 ID 저장
                        .email(email)
                        .nickname(String.valueOf(kakaoId)) // ✅ 닉네임을 카카오 ID로 설정
                        .authType(AuthType.KAKAO) // ✅ 카카오 로그인 방식
                        .password(null)  // ✅ 카카오는 비밀번호 없음
                        .role(Member.Role.일반회원)  // ✅ 기본 회원 권한 설정
                        .status(Member.MemberStatus.ACTIVE)  // ✅ 활성 계정으로 설정
                        .createdAt(createdAt) // ✅ 카카오 `connected_at` 값 사용
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
