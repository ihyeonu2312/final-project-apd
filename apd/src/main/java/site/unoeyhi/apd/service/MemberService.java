package site.unoeyhi.apd.service;

import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ✅ 회원 ID로 회원 찾기 (CartController에서 사용)
    public Optional<Member> findById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    // ✅ 이메일로 회원 찾기 (추가)
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // ✅ 회원가입 (비밀번호 암호화)
    public Member registerMember(String name, String email, String password, String nickname, String phoneNumber, String address, String detailAdd) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        // ✅ 입력값 검증 (name, detailAdd 필수 입력)
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("이름을 입력해야 합니다.");
        }
        if (detailAdd == null || detailAdd.isEmpty()) {
            throw new RuntimeException("상세 주소를 입력해야 합니다.");
        }

        String encryptedPassword = passwordEncoder.encode(password);

        Member member = Member.builder()
                .name(name)  // ✅ 추가
                .email(email)
                .password(encryptedPassword)
                .nickname(nickname)
                .phone(phoneNumber)
                .address(address)
                .detailAdd(detailAdd)  // ✅ 추가
                .role(Member.Role.일반회원)  
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return memberRepository.save(member);
    }

    // ✅ 로그인 (JWT 토큰 발급)
    public String loginMember(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        return jwtUtil.generateToken(email); // JWT 토큰 생성 후 반환
    }

    // ✅ 회원 저장
    public Member save(Member member) {
        log.info("Saving member with ID: {}", member.getMemberId());
        return memberRepository.save(member);  // 실제 DB에 저장하는 코드
    }
}
