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
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getNickname(),
                member.getPhone(),
                member.getAddress(),
                member.getDetailAdd()
            );
            return ResponseEntity.ok("회원가입 성공!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
