package site.unoeyhi.apd.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "kakao_verification")  // ✅ 테이블명 매칭
public class KakaoVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kakao_verification_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)  // ✅ Member 테이블과 연결
    private Member member;

    @Column(name = "kakao_access_token", nullable = false, unique = true)
    private String kakaoAccessToken;

    @Column(name = "kakao_refresh_token", nullable = false)
    private String kakaoRefreshToken;

    @Column(name = "status", nullable = false)
    private String status;  // ✅ ACTIVE, EXPIRED 등의 상태 값 저장

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ✅ 생성자 추가
    public KakaoVerification(Member member, String kakaoAccessToken, String kakaoRefreshToken, String status) {
        this.member = member;
        this.kakaoAccessToken = kakaoAccessToken;
        this.kakaoRefreshToken = kakaoRefreshToken;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}
