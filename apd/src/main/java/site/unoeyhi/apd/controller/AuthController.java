package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import site.unoeyhi.apd.util.JwtUtil;
import site.unoeyhi.apd.entity.dto.LoginRequest;
import site.unoeyhi.apd.entity.dto.SignupRequest;
import site.unoeyhi.apd.service.EmailService;
import site.unoeyhi.apd.service.MemberService;
import site.unoeyhi.apd.entity.dto.AuthResponse;
import site.unoeyhi.apd.entity.dto.EmailVerificationRequest;
import site.unoeyhi.apd.entity.Member;

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
            // 사용자 인증 수행
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT 생성
            String jwt = jwtUtil.generateToken(authentication.getName());

            // 응답 반환
            return ResponseEntity.ok(new AuthResponse(jwt));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null)); // ❌ 로그인 실패 시 null 반환
        }
    }

    // ✅ 회원가입 API (회원가입 후 JWT 토큰 반환)
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        try {
            // 회원가입 수행
            Member newMember = memberService.registerMember(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                request.getPhoneNumber(),
                request.getAddress(),
                request.getDetailAddress()
            );

            // 회원가입 후 자동 로그인 (JWT 생성)
            String jwt = jwtUtil.generateToken(newMember.getEmail());

            return ResponseEntity.ok(new AuthResponse(jwt));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null)); // ❌ 이메일 중복 등의 문제 발생 시 null 반환
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new AuthResponse(null)); // ❌ 서버 오류 발생 시 null 반환
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
