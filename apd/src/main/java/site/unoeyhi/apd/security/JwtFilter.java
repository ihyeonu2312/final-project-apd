package site.unoeyhi.apd.security;

import site.unoeyhi.apd.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Log4j2  
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

        // ✅ 요청 URL 로그 출력
        String requestURI = request.getRequestURI();
        log.info("🔍 요청 URL: {}", requestURI);

        // ✅ 인증이 필요하지 않은 API 목록 (회원가입, 로그인, 이메일 인증 API 추가)
        List<String> excludedUrls = List.of(
                "/api/auth/login",
                "/api/auth/signup",
                "/api/auth/send-email",
                "/api/auth/verify-email",
                "/api/address/search"
        );

        // ✅ 해당 URL이면 필터 통과 (인증 제외)
        if (excludedUrls.stream().anyMatch(requestURI::startsWith)) {
            log.info("🟢 인증 제외 API 요청 - JWT 인증 제외: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // 🔹 헤더에서 Authorization 가져오기
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 제거
            log.debug("✅ JWT 토큰 감지: {}", token); // ✅ 토큰 값 로그 출력

            try {
                if (jwtUtil.validateToken(token)) { // 🔥 토큰 검증 추가
                    String email = jwtUtil.extractEmail(token); // 🔥 이메일 추출
                    log.info("🔐 인증된 사용자 이메일: {}", email);

                    // 🔹 사용자 정보 로드 (DB에서 가져오기)
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // 🔥 Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication); // 🔥 인증 정보 저장
                    log.info("✅ SecurityContext에 사용자 등록 완료!");
                } else {
                    log.warn("🚨 JWT 검증 실패: 유효하지 않은 토큰");
                }
            } catch (Exception e) {
                log.error("❌ JWT 필터에서 오류 발생: ", e);
            }
        } else {
            log.warn("🚨 Authorization 헤더 없음 또는 형식 오류");
        }

        chain.doFilter(request, response);
    }
}
