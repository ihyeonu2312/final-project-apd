package site.unoeyhi.apd.controller;

import site.unoeyhi.apd.dto.UpdateUserRequest;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.service.EmailService;
import site.unoeyhi.apd.service.MemberService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor  // 🔥 경로 변경: /api/user 로 설정
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final EmailService emailService;

    // ✅ 특정 회원 조회 (기존 코드)
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ 현재 로그인한 회원 정보 조회 API 추가
    @GetMapping("/profile")
    public ResponseEntity<MemberProfileDto> getProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
    
        String subject = authentication.getName();
        log.info("🔍 현재 로그인한 사용자: {}", subject);
    
        Optional<Member> member;
    
        if (subject.contains("@")) {
            member = memberService.findByEmail(subject);
        } else {
            member = memberService.findByKakaoId(Long.parseLong(subject));
        }
    
        return member.map(m -> {
            MemberProfileDto dto = new MemberProfileDto(
                m.getMemberId(),
                m.getEmail(),
                m.getName(),
                m.getNickname(),
                m.getPhoneNumber(),
                m.getAddress(),
                m.getDetailAddress(),
                m.getRole().name(),
                m.getStatus().name(),
                m.getAuthType().name()
            );
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.status(404).build());
    }
    

 
    //     @GetMapping("/check-email")
    //     public ResponseEntity<String> checkEmailExists(@RequestParam String email) {
    //     boolean exists = emailService.checkEmailExists(email);
    //     return exists ? ResponseEntity.ok("EXISTS") : ResponseEntity.ok("NOT_EXISTS");
    // }

    // ✅ 이메일 중복 확인 API (findByEmail 사용)
    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        boolean exists = memberRepository.findByEmail(email).isPresent();
        return ResponseEntity.ok(exists ? "EXISTS" : "AVAILABLE");
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


     /* 🔹 회원 정보 수정 API */
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,  // 현재 로그인한 사용자 정보
            @RequestBody UpdateUserRequest request) {

        String email = userDetails.getUsername(); // 로그인한 사용자의 이메일 가져오기
        boolean updated = memberService.updateUser(email, request);

        if (updated) {
            return ResponseEntity.ok("회원 정보 수정 성공!");
        } else {
            return ResponseEntity.status(400).body("회원 정보 수정 실패");
        }
    }

 }

