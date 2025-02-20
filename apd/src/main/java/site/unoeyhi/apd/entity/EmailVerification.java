package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true) // ✅ 이메일 필드 추가
    private String email; // ✅ 회원가입 전에도 이메일 기준으로 저장 가능

    @Column(name = "member_id") // 회원가입 후 연결 가능하도록 nullable 허용
    private Long memberId;

    @Column(name = "verification_token", nullable = false, unique = true)
    private String verificationToken; // 인증코드랜덤생성

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmailVerificationStatus status = EmailVerificationStatus.PENDING; //초기 상태 PENDING

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt; // 만료시간 설정

    public enum EmailVerificationStatus {
        PENDING, VERIFIED, EXPIRED
    }
}
