package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import site.unoeyhi.apd.entity.EmailVerification;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByMemberId(Long memberId);
    Optional<EmailVerification> findByEmail(String email); // ✅ 이메일 기준 조회 추가
    Optional<EmailVerification> findByVerificationToken(String token); // ✅ 토큰 기준 조회 추가

 
    @Transactional
    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.expiredAt < :now")
    int deleteByExpiredAtBefore(@Param("now") LocalDateTime now);
}