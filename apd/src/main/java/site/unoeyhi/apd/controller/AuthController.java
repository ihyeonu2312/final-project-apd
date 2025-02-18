package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Claims;
import site.unoeyhi.apd.util.JwtUtil;
import site.unoeyhi.apd.entity.dto.LoginRequest;
import site.unoeyhi.apd.entity.dto.SignupRequest;
import site.unoeyhi.apd.service.EmailService;
import site.unoeyhi.apd.service.MemberService;
import site.unoeyhi.apd.entity.dto.AuthResponse;
import site.unoeyhi.apd.entity.dto.EmailVerificationRequest;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MemberService memberService;

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

  // ✅ 개인정보 동의 API (JWT 발급 방식)
  @PostMapping("/consent")
  public ResponseEntity<String> agreeToConsent(@RequestBody Map<String, Boolean> request) {
      Boolean consentAgreed = request.get("consentAgreed");

      if (consentAgreed == null || !consentAgreed) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("개인정보 동의가 필요합니다.");
      }

      // ✅ 개인정보 동의용 JWT 발급 (10분 만료)
      String consentToken = jwtUtil.generateTokenWithClaims("CONSENT_AGREED", true, 10 * 60 * 1000);

      return ResponseEntity.ok(consentToken);
  }

  // ✅ 개인정보 동의 확인 API (JWT 기반 검증)
  @GetMapping("/check-consent")
  public ResponseEntity<?> checkConsent(@RequestHeader("Authorization") String token) {
      try {
          if (token == null || !token.startsWith("Bearer ")) {
              return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: No valid token found.");
          }

          // ✅ JWT에서 동의 상태 확인
          Claims claims = jwtUtil.parseToken(token.replace("Bearer ", ""));
          Boolean consentAgreed = (Boolean) claims.get("CONSENT_AGREED");

          if (consentAgreed == null || !consentAgreed) {
              return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Consent not agreed.");
          }

          return ResponseEntity.ok("CONSENT_GRANTED");
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid token.");
      }
  }

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody SignupRequest request, @RequestHeader("Authorization") String token) {
      try {
          if (token == null || !token.startsWith("Bearer ")) {
              return ResponseEntity.status(HttpStatus.FORBIDDEN).body("개인정보 동의가 필요합니다.");
          }

          Claims claims = jwtUtil.parseToken(token.replace("Bearer ", ""));
          Boolean consentAgreed = (Boolean) claims.get("CONSENT_AGREED");

          if (consentAgreed == null || !consentAgreed) {
              return ResponseEntity.status(HttpStatus.FORBIDDEN).body("개인정보 동의가 필요합니다.");
          }
            // ✅ 회원가입 수행
            memberService.registerMember(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                request.getPhoneNumber(),
                request.getAddress(),
                request.getDetailAddress()
            );

            return ResponseEntity.ok("회원가입 성공!");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류로 회원가입 실패!");
        }
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
}
