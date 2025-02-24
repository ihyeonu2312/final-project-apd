package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import site.unoeyhi.apd.util.JwtUtil;
import site.unoeyhi.apd.dto.AuthResponse;
import site.unoeyhi.apd.dto.EmailVerificationRequest;
import site.unoeyhi.apd.dto.LoginRequest;
import site.unoeyhi.apd.dto.SignupRequest;
import site.unoeyhi.apd.entity.EmailVerification;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.EmailVerification.EmailVerificationStatus;
import site.unoeyhi.apd.entity.Member.AuthType;
import site.unoeyhi.apd.repository.EmailVerificationRepository;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.service.EmailService;
import site.unoeyhi.apd.service.KakaoAuthService;
import site.unoeyhi.apd.service.MemberService;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${KAKAO_CLIENT_ID}")
    private String KAKAO_CLIENT_ID;

    @Value("http://localhost:8080/api/auth/kakao/callback")
    private String REDIRECT_URI;

    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MemberService memberService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final KakaoAuthService kakaoAuthService;

    // ✅ 로그인 API (JWT 발급)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ JWT 생성 후 반환
            String jwt = jwtUtil.generateToken(authentication.getName());
            return ResponseEntity.ok(new AuthResponse(jwt));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null));
        }
    }


@PostMapping("/signup")
public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
    Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByEmail(request.getEmail());

    if (verificationOpt.isEmpty() || verificationOpt.get().getStatus() != EmailVerificationStatus.VERIFIED) {
        return ResponseEntity.status(403).body("이메일 인증이 완료되지 않았습니다.");
    }

    // ✅ 이미 가입된 이메일인지 확인
    if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
        return ResponseEntity.status(409).body("이미 가입된 이메일입니다.");
    }

    memberService.registerMember(
        request.getName(),
        request.getEmail(),
        request.getPassword(),
        request.getNickname(),
        request.getPhoneNumber(),
        request.getAddress(),
        request.getDetailAddress(),
        AuthType.EMAIL
    );

    return ResponseEntity.ok("회원가입 성공!");
}

@PostMapping("/logout")
public ResponseEntity<String> logout() {
    SecurityContextHolder.clearContext(); // 🔥 Spring Security 컨텍스트 초기화
    return ResponseEntity.ok("로그아웃 성공!");
}

    // ✅ 이메일 인증 요청 API
    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailVerificationRequest request) {
        try {
            emailService.sendVerificationEmail(request.getEmail());
            return ResponseEntity.ok("인증 이메일이 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이메일 전송 실패: " + e.getMessage());
        }
    }

  //  ✅ 이메일 인증 확인 API
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        boolean isVerified = emailService.verifyEmail(token);

        if (isVerified) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("유효하지 않거나 만료된 인증 코드입니다.");
        }
    }

        // ✅ 프론트에서 카카오 로그인 요청 시 실행되는 엔드포인트
        @GetMapping("/kakao/login")
        public ResponseEntity<String> kakaoLogin() {
            String redirectUrl = "https://kauth.kakao.com/oauth/authorize"
                    + "?client_id=" + KAKAO_CLIENT_ID
                    + "&redirect_uri=" + REDIRECT_URI
                    + "&response_type=code";
    
            return ResponseEntity.ok(redirectUrl);
        }
    
        @PostMapping("/kakao/callback")
        public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code) {
            String tokenUrl = "https://kauth.kakao.com/oauth/token"
                    + "?grant_type=authorization_code"
                    + "&client_id=" + KAKAO_CLIENT_ID
                    + "&redirect_uri=" + REDIRECT_URI
                    + "&code=" + code;
        
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, null, String.class);
        
            // ✅ 응답 값 출력 확인 (디버깅용)
            System.out.println("카카오 응답: " + response.getBody());
        
            // ✅ JSON 파싱 및 access_token 추출
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
        
                String accessToken = jsonNode.get("access_token").asText(); // 🔥 access_token 가져오기
        
                // ✅ JWT 생성 후 반환 (Spring Security + JWT 활용)
                String jwtToken = jwtUtil.generateToken(accessToken);
        
                // ✅ 클라이언트에 토큰 반환
                return ResponseEntity.ok(new AuthResponse(jwtToken));
        
            } catch (Exception e) {
                return ResponseEntity.status(500).body("카카오 로그인 실패: " + e.getMessage());
            }
        }
}