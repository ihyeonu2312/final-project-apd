package site.unoeyhi.apd.controller;

import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor  // 🔥 경로 변경: /api/user 로 설정
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    // ✅ 특정 회원 조회 (기존 코드)
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ 현재 로그인한 회원 정보 조회 API 추가
    @GetMapping("/profile")
    public ResponseEntity<Member> getProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();  // 인증되지 않은 경우 401 반환
        }

        String email = authentication.getName();  // 현재 로그인한 사용자의 이메일 가져오기
        Optional<Member> member = memberService.findByEmail(email);

        return member.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build()); // 회원이 존재하지 않으면 404 반환
    }

 
    // ✅ 닉네임 중복 확인 API
     @GetMapping("/check-nickname")
     public ResponseEntity<String> checkNickname(@RequestParam String nickname) {
         boolean exists = memberRepository.findByNickname(nickname).isPresent();
         return ResponseEntity.ok(exists ? "EXISTS" : "AVAILABLE");
     }
 
    // ✅ 전화번호 중복 확인 API
    @GetMapping("/check-phone")
    public ResponseEntity<String> checkPhone(@RequestParam String phoneNumber) {
        boolean exists = memberRepository.findByPhoneNumber(phoneNumber).isPresent();
        return ResponseEntity.ok(exists ? "EXISTS" : "AVAILABLE");
    }
 }

