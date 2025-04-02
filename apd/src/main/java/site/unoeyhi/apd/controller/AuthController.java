package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import site.unoeyhi.apd.util.JwtUtil;
import site.unoeyhi.apd.dto.AuthResponse;
import site.unoeyhi.apd.dto.EmailVerificationRequest;
import site.unoeyhi.apd.dto.LoginRequest;
import site.unoeyhi.apd.dto.MemberDto;

import site.unoeyhi.apd.dto.ResetPasswordRequestDto;
import site.unoeyhi.apd.dto.SignupRequest;
import site.unoeyhi.apd.entity.EmailVerification;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.EmailVerification.EmailVerificationStatus;
import site.unoeyhi.apd.entity.Member.AuthType;
import site.unoeyhi.apd.repository.EmailVerificationRepository;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.security.CustomUserDetails;
import site.unoeyhi.apd.service.EmailService;
import site.unoeyhi.apd.service.KakaoAuthService;
import site.unoeyhi.apd.service.MemberService;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  @Value("${kakao.client-id}")
  private String kakaoClientId;

  @Value("${kakao.redirect-uri}")
  private String kakaoRedirectUri;

  private final EmailService emailService;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final MemberService memberService;
  private final EmailVerificationRepository emailVerificationRepository;
  private final MemberRepository memberRepository;
  private final KakaoAuthService kakaoAuthService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
      try {
          Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
          );
  
          SecurityContextHolder.getContext().setAuthentication(authentication);
  
          // ✅ 로그인한 사용자 정보 조회
          Optional<Member> member = memberRepository.findByEmail(authentication.getName());
  
          if (member.isEmpty()) {
              return ResponseEntity.badRequest().body(new AuthResponse(null, null));
          }
  
          // ✅ 역할 포함된 JWT 발급
          String jwt = jwtUtil.generateToken(member.get().getEmail(), member.get().getRole().name());
  
          return ResponseEntity.ok(new AuthResponse(jwt, member.get().getMemberId()));
      } catch (Exception e) {
          return ResponseEntity.badRequest().body(new AuthResponse(null, null));
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
        AuthType.EMAIL);

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

  // ✅ 이메일 인증 확인 API
  @GetMapping("/verify-email")
  public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
    boolean isVerified = emailService.verifyEmail(token);

    if (isVerified) {
      return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    } else {
      return ResponseEntity.badRequest().body("유효하지 않거나 만료된 인증 코드입니다.");
    }
  }

  // 비밀번호 재설정
  @PostMapping("/reset-password")
  public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDto request) {
      Optional<Member> memberOpt = memberRepository.findByEmail(request.getEmail());
  
      if (memberOpt.isEmpty()) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
      }
  
      Member member = memberOpt.get();
  
      try {
          memberService.updatePassword(member, request.getNewPassword()); // ✅ 이메일 인증 확인 후 비밀번호 변경
          return ResponseEntity.ok("비밀번호 변경 성공!");
      } catch (IllegalStateException e) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).body("이메일 인증이 완료되지 않았습니다.");
      }
  }
  



  // ✅ 프론트에서 카카오 로그인 URL 요청 시 실행
  @GetMapping("/kakao/login")
  public ResponseEntity<Map<String, String>> kakaoLogin() {
    String redirectUrl = "https://kauth.kakao.com/oauth/authorize"
        + "?client_id=" + kakaoClientId
        + "&redirect_uri=" + kakaoRedirectUri
        + "&response_type=code";

    Map<String, String> response = new HashMap<>();
    response.put("redirectUrl", redirectUrl);

    return ResponseEntity.ok(response);
  }

  // ✅ 카카오 로그인 콜백 처리 (인가 코드 → 액세스 토큰 요청)
  @GetMapping("/kakao/callback")
  public void kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
    log.info("🔥 카카오 로그인 코드 수신: {}", code);
  
      try {
          log.info("🔍 카카오 API에 액세스 토큰 요청 시작...");
  
          // ✅ 카카오에서 액세스 토큰 및 리프레시 토큰 가져오기
          Map<String, String> tokens = kakaoAuthService.getTokensFromKakao(code);
  
          log.info("✅ 카카오 API 응답 수신 완료: {}", tokens);
  
          String accessToken = tokens.get("access_token");
          String refreshToken = tokens.get("refresh_token");
  
          log.info("🔥 카카오 액세스 토큰: {}", accessToken);
          log.info("🔥 카카오 리프레시 토큰: {}", refreshToken);
  
          log.info("🔍 카카오 사용자 정보 조회 시작...");
          Member member = kakaoAuthService.getOrCreateKakaoUser(accessToken, refreshToken);
          log.info("✅ 사용자 정보 조회 완료: {}", member);
  
          // ✅ JWT 토큰 생성 (회원 로그인)
          // ✅ 역할 정보 포함하여 JWT 생성
String jwtToken = jwtUtil.generateTokenForKakao(member.getKakaoId(), member.getRole().name());

 
          log.info("✅ JWT 발급 완료: {}", jwtToken);
          // ✅ 프론트엔드 `/kakao/callback?token=JWT값`으로 리디렉트
          response.sendRedirect("http://localhost:5173/kakao/callback?token=" + jwtToken);
      } catch (Exception e) {
          log.error("❌ 카카오 로그인 처리 중 오류 발생", e);
          response.sendRedirect("http://localhost:5173/login?error=카카오 로그인 실패");
      }
  }
  @GetMapping("/me") // ✅ 최종 엔드포인트는 "/api/auth/me"
  public ResponseEntity<MemberDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
      if (userDetails == null) {
          return ResponseEntity.status(401).build(); // 로그인되지 않은 경우 401 응답
      }
      MemberDto response = new MemberDto(userDetails.getMemberId(), userDetails.getEmail(), userDetails.getRole());
      return ResponseEntity.ok(response);
  }
}