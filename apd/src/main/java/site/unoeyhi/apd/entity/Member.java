package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id") // ✅ DB 컬럼명과 매칭
    private Long memberId;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address" , nullable = false)
    private String address;

    @Column(name = "detail_address" , nullable = false)
    private String detailAdd;


    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.일반회원;


    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)  // Enum을 저장할 때 문자열로 저장
    private MemberStatus status = MemberStatus.INACTIVE ;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt = LocalDateTime.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_password_change", nullable = false)
    private LocalDateTime lastPass = LocalDateTime.now();

    public enum Role {
        일반회원, 관리자
    }

    public enum MemberStatus{
        ACTIVE,    // 접속 중
        INACTIVE   // 접속 중 아님
    }
}
