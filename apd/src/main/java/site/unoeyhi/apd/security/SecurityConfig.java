package site.unoeyhi.apd.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()  // API 서버에서는 CSRF 필요 없음
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()  // 로그인, 회원가입은 인증 없이 허용
                .anyRequest().authenticated()  // 나머지는 인증 필요
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);  // JWT 필터 추가

        return http.build();
    }
}
//SecurityConfig를 Spring Security 6.x 버전에 맞게 변경
// UsernamePasswordAuthenticationFilter 앞에서 JwtFilter가 실행되도록 설정
// PasswordEncoder를 Bean으로 등록