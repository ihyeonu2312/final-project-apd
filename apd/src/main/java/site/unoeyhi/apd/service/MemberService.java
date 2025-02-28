package site.unoeyhi.apd.service;

import site.unoeyhi.apd.entity.EmailVerification;
import site.unoeyhi.apd.entity.EmailVerification.EmailVerificationStatus;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.entity.Member.AuthType;
import site.unoeyhi.apd.entity.Member.MemberStatus;
import site.unoeyhi.apd.repository.EmailVerificationRepository;
import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final EmailVerificationRepository emailVerificationRepository;  // ✅ 필드 추가!
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

    //kakao 회원찾기
    public Optional<Member> findByKakaoId(Long kakaoId) {
        return memberRepository.findByKakaoId(kakaoId);
    }

    // ✅ 회원가입 (비밀번호 암호화 및 검증 강화)
    @Transactional
    public Member registerMember(String name, String email, String password, String nickname, String phoneNumber, String address, String detailAddress, AuthType authType) {
        // 중복 체크
        validateDuplicateMember(email, nickname, phoneNumber);

        // 필수 입력값 검증
        validateInputFields(name, email, password, detailAddress);

        // 비밀번호 암호화
        String encryptedPassword = (authType == AuthType.EMAIL) ? passwordEncoder.encode(password) : null;

        // ✅ 이메일 인증 여부 확인 (email_verification 테이블 조회)
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByEmail(email);
        
        
        // 🔥 isVerified 변수 선언 (이제 에러 없음!)
        boolean isVerified = verificationOpt.isPresent() && verificationOpt.get().getStatus() == EmailVerificationStatus.VERIFIED;

        if (!isVerified) {
            log.warn("❌ 이메일 인증이 완료되지 않은 상태에서 회원가입 시도 - 이메일: {}", email);
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다. 인증 후 회원가입을 진행하세요.");
        }

        MemberStatus memberStatus = MemberStatus.ACTIVE;

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
                .authType(authType)
                .status(memberStatus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

                log.info("🔥 회원가입 완료 - 이메일: {}, 상태: {}", email, memberStatus);

        return memberRepository.save(member);
    }

    // ✅ 로그인 (JWT 토큰 발급)
    public String loginMember(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        return jwtUtil.generateToken(member.getEmail()); // JWT 토큰 생성 후 반환
    }

    @Transactional
    public void updatePassword(Member member, String newPassword) {
        // ✅ 이메일 인증 상태 확인
        Optional<EmailVerification> emailVerificationOpt = emailVerificationRepository.findByEmail(member.getEmail());
    
        if (emailVerificationOpt.isEmpty() || emailVerificationOpt.get().getStatus() != EmailVerificationStatus.VERIFIED) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }
    
        // ✅ 새 비밀번호 암호화 후 저장
        String encryptedPassword = passwordEncoder.encode(newPassword);
        member.setPassword(encryptedPassword);
        member.setUpdatedAt(LocalDateTime.now());
    
        memberRepository.save(member);
    }
    
    

    // ✅ 회원 저장
    public Member save(Member member) {
        log.info("Saving member with ID: {}", member.getMemberId());
        return memberRepository.save(member);
    }


    // 🔹 중복 회원 체크 로직
    private void validateDuplicateMember(String email, String nickname, String phoneNumber) {
        if (memberRepository.findByEmail(email).isPresent()) {
            log.warn("❌ 중복된 이메일로 회원가입 시도: {}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }
        if (memberRepository.findByNickname(nickname).isPresent()) {
            log.warn("❌ 중복된 닉네임으로 회원가입 시도: {}", nickname);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }
        if (memberRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            log.warn("❌ 중복된 전화번호로 회원가입 시도: {}", phoneNumber);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다.");
        }
    }

    // 🔹 필수 입력값 검증 로직
    private void validateInputFields(String name, String email, String password, String detailAdd) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이름을 입력해야 합니다.");
        }
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일을 입력해야 합니다.");
        }
        if (password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 최소 6자리 이상이어야 합니다.");
        }
        if (detailAdd == null || detailAdd.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상세 주소를 입력해야 합니다.");
        }
    }
}
