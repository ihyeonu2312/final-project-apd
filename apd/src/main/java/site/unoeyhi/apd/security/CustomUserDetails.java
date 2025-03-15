package site.unoeyhi.apd.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import site.unoeyhi.apd.entity.Member;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Long memberId;   // ✅ 회원 ID
    private final String email;     // ✅ 이메일
    private final String password;  // ✅ 비밀번호
    private final String role;      // ✅ 역할 (일반회원, 관리자)
    private final Member member;    // ✅ Member 엔티티 직접 저장

    public CustomUserDetails(Member member) {
        this.member = member;
        this.memberId = member.getMemberId();
        this.email = member.getEmail();
        this.password = member.getPassword();
        this.role = member.getRole().name(); // Enum -> String 변환
    }

    // ✅ 이메일 반환
    public String getEmail() {
        return this.email;
    }

    // ✅ 역할 반환
    public String getRole() {
        return this.role;
    }

    // ✅ 회원 ID 반환
    public Long getMemberId() {
        return this.memberId;
    }

    // ✅ Member 엔티티 반환
    public Member getMember() {
        return this.member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 필요시 권한 추가
    }

    @Override
    public String getPassword() {
        return member.getPassword() != null ? member.getPassword() : "";
    }

    @Override
    public String getUsername() {
        return member.getEmail() != null ? member.getEmail() : String.valueOf(member.getKakaoId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
