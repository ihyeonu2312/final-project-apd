package site.unoeyhi.apd.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

// JwtAuthentication은 Spring Security의 AbstractAuthenticationToken을 확장
public class JwtAuthentication extends AbstractAuthenticationToken {
    private final String email; // 인증된 사용자의 이메일 정보

    // 생성자
    public JwtAuthentication(String email) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))); // 기본 권한 설정
        this.email = email;
        setAuthenticated(true); // 인증된 상태로 설정
    }

    // 사용자 비밀번호 또는 추가적인 인증 정보를 반환하는 메서드
    @Override
    public Object getCredentials() {
        return null; // JWT 인증에서는 비밀번호 정보는 필요 없으므로 null로 설정
    }

    // 인증된 사용자의 기본 정보(여기서는 이메일)
    @Override
    public Object getPrincipal() {
        return email;
    }

    // 이메일 정보 반환
    public String getEmail() {
        return email;
    }
}
