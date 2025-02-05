package com.alpangdang.controller;

import com.alpangdang.entity.Member;
import com.alpangdang.service.MemberService;
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
    public ResponseEntity<String> register(@RequestParam String email,
                                           @RequestParam String password,
                                           @RequestParam String nickname) {
        memberService.registerMember(email, password, nickname);
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