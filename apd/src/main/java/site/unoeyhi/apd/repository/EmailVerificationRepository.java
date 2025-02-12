package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.unoeyhi.apd.entity.EmailVerification;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByMemberId(Long memberId);
    Optional<EmailVerification> findByEmail(String email); // ✅ 이메일 기준 조회 추가
    Optional<EmailVerification> findByVerificationToken(String token); // ✅ 토큰 기준 조회 추가
}
