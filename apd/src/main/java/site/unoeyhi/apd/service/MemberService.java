package site.unoeyhi.apd.service;

import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public MemberService(MemberRepository memberRepository, JwtUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
    }
    //CartController 조회에서 사용 목적
    public Optional<Member> findById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public Member registerMember(String email, String password, String nickname, String phoneNumber, String address) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        String encryptedPassword = passwordEncoder.encode(password);
        
        Member member = Member.builder()
                .email(email)
                .password(encryptedPassword)
                .nickname(nickname)
                .phone(phoneNumber)
                .address(address)
                .role(Member.Role.일반회원)  // 기본값 일반회원
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    
        return memberRepository.save(member);
    }
    


    public String loginMember(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        return jwtUtil.generateToken(email); // JWT 토큰 생성 후 반환
    }
    public Member save(Member member) {
        log.info("Saving member with ID: {}", member.getMemberId());
        return memberRepository.save(member);  // 실제 DB에 저장하는 코드
    }

}
// ✔️ registerMember() → 회원가입 (비밀번호 암호화)
// ✔️ loginMember() → JWT 토큰 발급

