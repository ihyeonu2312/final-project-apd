package site.unoeyhi.apd.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.unoeyhi.apd.entity.Member;
import site.unoeyhi.apd.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;



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
    
        //return User.builder()
               // .username(foundMember.getEmail() != null ? foundMember.getEmail() : String.valueOf(foundMember.getKakaoId()))
               // .password(foundMember.getPassword() != null ? foundMember.getPassword() : "")
               // .roles(foundMember.getRole().name())
               // .build();
               return member.map(CustomUserDetails::new)
               .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + identifier));
    }
    public Long getAuthenticatedMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getMemberId();
        }
        throw new AccessDeniedException("로그인이 필요합니다.");
    }

}
