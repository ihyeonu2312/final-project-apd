package site.unoeyhi.apd.security;

import site.unoeyhi.apd.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 🔥 @Lazy를 UserDetailsService에 적용해서 순환참조 해결
    public JwtFilter(JwtUtil jwtUtil, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        // 🔹 헤더에서 Authorization 가져오기
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 제거
            if (jwtUtil.validateToken(token)) { // 🔥 토큰 검증 추가
                String email = jwtUtil.extractEmail(token); // 🔥 이메일 추출

                // 🔹 사용자 정보 로드 (DB에서 가져오기)
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                // 🔥 Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication); // 🔥 인증 정보 저장
            }
        }

        chain.doFilter(request, response);
    }
}
