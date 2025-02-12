package site.unoeyhi.apd.service;

import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // ✅ 회원 ID로 회원 찾기 (CartController에서 사용)
    public Optional<Member> findById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    // ✅ 이메일로 회원 찾기
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // ✅ 회원가입 (비밀번호 암호화 및 검증 강화)
    @Transactional
    public Member registerMember(String name, String email, String password, String nickname, String phoneNumber, String address, String detailAddress) {
        // 중복 체크
        validateDuplicateMember(email, nickname, phoneNumber);

        // 필수 입력값 검증
        validateInputFields(name, email, password, detailAddress);

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(password);

        // 회원 객체 생성 및 저장
        Member member = Member.builder()
                .name(name)
                .email(email)
                .password(encryptedPassword)
                .nickname(nickname)
                .phoneNumber(phoneNumber)
                .address(address)
                .detailAddress(detailAddress)
                .role(Member.Role.일반회원)  // 기본 역할 설정
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return memberRepository.save(member);
    }

    // ✅ 로그인 (JWT 토큰 발급)
    public String loginMember(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtUtil.generateToken(email); // JWT 토큰 생성 후 반환
    }

    // ✅ 회원 저장
    public Member save(Member member) {
        log.info("Saving member with ID: {}", member.getMemberId());
        return memberRepository.save(member);
    }

    // 🔹 중복 회원 체크 로직
    private void validateDuplicateMember(String email, String nickname, String phoneNumber) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.findByNickname(nickname).isPresent()) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }
        if (memberRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new RuntimeException("이미 사용 중인 전화번호입니다.");
        }
    }

    // 🔹 필수 입력값 검증 로직
    private void validateInputFields(String name, String email, String password, String detailAdd) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("이름을 입력해야 합니다.");
        }
        if (email == null || email.isBlank()) {
            throw new RuntimeException("이메일을 입력해야 합니다.");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("비밀번호는 최소 6자리 이상이어야 합니다.");
        }
        if (detailAdd == null || detailAdd.isBlank()) {
            throw new RuntimeException("상세 주소를 입력해야 합니다.");
        }
    }
}
