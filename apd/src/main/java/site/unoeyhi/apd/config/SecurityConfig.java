package site.unoeyhi.apd.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import lombok.RequiredArgsConstructor;
import site.unoeyhi.apd.security.CustomUserDetailsService;
import site.unoeyhi.apd.security.JwtFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter; // JWT 필터 주입
    private final CustomUserDetailsService customUserDetailsService; // 🔥 사용자 정보 로드 서비스 추가

    // 🔹 비밀번호 암호화를 위한 BCryptPasswordEncoder 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔹 AuthenticationManager 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 🔹 UserDetailsService 빈 등록
    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }

    // 🔹 사용자 인증 제공자 (AuthenticationProvider) 설정
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService()); // 🔥 커스텀 UserDetailsService 사용
        authProvider.setPasswordEncoder(passwordEncoder()); // 비밀번호 암호화 적용
        return authProvider;
    }

    // 🔹 Spring Security 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (API 서버에서는 주로 비활성화)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login","api/auth/kakao/login",
                "api/auth/kakao/callback", "/api/auth/signup", "/api/auth/send-email",
                "/api/auth/verify-email", "/api/user/check-email", "api/auth/reset-password").permitAll()//로그인 & 회원가입 & 이메일 인증 API 허용
                .requestMatchers("/api/auth/**").authenticated() // 
                .requestMatchers("/api/aliexpress/scrap").permitAll() // 알리 스크랩 전체권한 허용
                .requestMatchers("/api/user/check-nickname", "/api/user/check-phone").permitAll()
                .requestMatchers("/api/public/**").permitAll() // 🔥 추가적인 공개 API 허용 가능
                .requestMatchers("/api/products").permitAll() // 🔥 `/api/products` 엔드포인트 접근 허용 추가
                .requestMatchers("/api/cart/**").permitAll() // 🔐 장바구니 API 인증 없이 사용가능하게
                .requestMatchers("/api/products/**").permitAll() // ✅ `/api/products/**` 전체 허용
                .requestMatchers("/api/products/category/**").permitAll() // ✅ 카테고리별 상품 API 전체 허용
                .requestMatchers("/api/categories/**").permitAll() // ✅ 카테고리 API 전체 허용
                .requestMatchers("/api/orders/**").permitAll()
                .requestMatchers( "/api/crawl/products").permitAll()  // 🔥 /api/products 엔드포인트 허용
                .requestMatchers("/api/address/search").permitAll() // ✅ 주소 검색 API는 인증 없이 허용
                .requestMatchers("/api/user/profile").authenticated() // ✅ 🔥 프로필 조회는 인증 필요
                .anyRequest().permitAll() // 🔐 그 외 모든 요청은 인증 필요
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

        return http.build();
    }

    // 🔹 CORS 설정 (프론트엔드와 연동할 때 필요)
    // 🔹 CORS 설정 (프론트엔드와 연동할 때 필요)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // ✅ 정확한 출처 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // ✅ 모든 HTTP 메소드 허용
        configuration.setAllowedHeaders(List.of("*")); // ✅ 모든 요청 헤더 허용
        configuration.setAllowCredentials(true); // ✅ 인증 정보 포함 허용
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie")); // ✅ 클라이언트가 JWT 토큰을 받을 수 있도록 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // ✅ 모든 경로에 대해 CORS 적용
        return source;
    }

}
