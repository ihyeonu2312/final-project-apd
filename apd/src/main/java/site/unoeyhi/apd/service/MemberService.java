package site.unoeyhi.apd.service;

import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    public Member registerMember(String email, String password, String nickname) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        String encryptedPassword = passwordEncoder.encode(password);
        Member member = new Member(null, email, encryptedPassword, nickname);
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

}
// ✔️ registerMember() → 회원가입 (비밀번호 암호화)
// ✔️ loginMember() → JWT 토큰 발급