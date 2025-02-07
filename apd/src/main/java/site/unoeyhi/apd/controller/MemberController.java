package site.unoeyhi.apd.controller;

import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Member member) {
        memberService.registerMember(
            member.getEmail(), 
            member.getPassword(), 
            member.getNickname(), 
            member.getPhone(), 
            member.getAddress()
        );
        return ResponseEntity.ok("회원가입 성공!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        String token = memberService.loginMember(email, password);
        return ResponseEntity.ok(token);
    }

}
// ✔️ /api/auth/register → 회원가입 API
// ✔️ /api/auth/login → 로그인 & JWT 발급
