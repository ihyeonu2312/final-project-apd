package site.unoeyhi.apd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.unoeyhi.apd.entity.KakaoVerification;
import java.util.Optional;

@Repository
public interface KakaoVerificationRepository extends JpaRepository<KakaoVerification, Long> {
    
    // ✅ 특정 회원(Member)의 카카오 인증 정보 조회
    Optional<KakaoVerification> findByMember_MemberId(Long memberId);

    // ✅ 특정 카카오 액세스 토큰이 있는지 조회
    Optional<KakaoVerification> findByKakaoAccessToken(String kakaoAccessToken);
}
