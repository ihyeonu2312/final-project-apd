package site.unoeyhi.apd.controller;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
import site.unoeyhi.apd.service.MemberService;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MemberService memberService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;

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
}
