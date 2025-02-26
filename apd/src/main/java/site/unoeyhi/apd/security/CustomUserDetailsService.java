package site.unoeyhi.apd.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<Member> member;
    
        if (identifier.contains("@")) { // ✅ 이메일 형식이면 이메일 로그인
            member = memberRepository.findByEmail(identifier);
        } else { // ✅ 숫자만 있으면 카카오 로그인 (kakao_id)
            member = memberRepository.findByKakaoId(Long.parseLong(identifier));
        }
    
        Member foundMember = member.orElseThrow(() -> 
            new UsernameNotFoundException("해당 사용자 정보를 찾을 수 없습니다: " + identifier));
    
        return User.builder()
                .username(foundMember.getEmail() != null ? foundMember.getEmail() : String.valueOf(foundMember.getKakaoId()))
                .password(foundMember.getPassword() != null ? foundMember.getPassword() : "")
                .roles(foundMember.getRole().name())
                .build();
    }
}
