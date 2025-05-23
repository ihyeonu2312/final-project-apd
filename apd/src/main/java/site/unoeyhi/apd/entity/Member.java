package site.unoeyhi.apd.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import site.unoeyhi.apd.entity.ChatRoom;
import site.unoeyhi.apd.entity.ChatMessage;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "kakao_id", unique = true)
    private Long kakaoId;  // ✅ 카카오 로그인 시 저장될 카카오 ID

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password") //, nullable = false
    private String password;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.일반회원;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.INACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    @Builder.Default
    private AuthType authType = AuthType.EMAIL;  // ✅ 기본값 EMAIL

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_password_change")
    @Builder.Default
    private LocalDateTime lastPass = LocalDateTime.now();

    public enum AuthType {
        EMAIL,  // 일반 이메일 로그인
        KAKAO   // 카카오 로그인
    }

    // ✅ 회원 상태 (권한)
    public enum Role {
        일반회원, 관리자
    }

    // ✅ 회원 활성 상태
    public enum MemberStatus {
        ACTIVE,   // 활성 계정
        INACTIVE  // 이메일 인증 미완료 (또는 제제 및 정지 비활성 계정)
    }

    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }


    // ✅ 자동으로 createdAt 설정 (최초 등록 시)
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastPass = LocalDateTime.now();
    }

    // ✅ 자동으로 updatedAt 갱신 (엔티티 수정 시)
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    private List<ChatRoom> buyingChatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<ChatRoom> sellingChatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<ChatMessage> sentMessages = new ArrayList<>();
}
