package site.unoeyhi.apd.controller;

import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // ✅ 회원가입 API
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member) {
        try {
            memberService.registerMember(
                member.getName(),  // ✅ 이름 추가
                member.getEmail(),
                member.getPassword(),
                member.getNickname(),
                member.getPhone(),
                member.getAddress(),
                member.getDetailAdd()  // ✅ 상세주소 추가
            );
            return ResponseEntity.ok("회원가입 성공!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ✅ 로그인 API (DTO 사용)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = memberService.loginMember(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}

// ✅ 로그인 요청을 위한 DTO 추가
class LoginRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
