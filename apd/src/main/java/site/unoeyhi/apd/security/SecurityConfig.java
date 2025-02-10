package site.unoeyhi.apd.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/cart/**").permitAll()  // /api/cart/** 경로는 인증 없이 접근 가능
                .anyRequest().authenticated()  // 그 외 요청은 인증 필요
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.permitAll())  // 로그인 폼 허용
            .logout(logout -> logout.permitAll());  // 로그아웃 허용

        return http.build();
    }
}