package site.unoeyhi.apd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import site.unoeyhi.apd.repository.MemberRepository;
import site.unoeyhi.apd.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private MemberRepository memberRepository;

    // Custom UserDetailsService
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Member member = memberRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return org.springframework.security.core.userdetails.User.builder()
                    .username(member.getEmail())
                    .password(member.getPassword())
                    .roles(member.getRole().name()) // 권한을 Role Enum에서 가져옴
                    .build();
        };
    }

    // 패스워드 인코딩을 위한 빈 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // HttpSecurity 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/cart/**").authenticated()  // 장바구니 API는 인증된 사용자만 접근 가능
                .antMatchers("/api/admin/**").hasRole("관리자")  // 관리자는 /api/admin/** 경로에 접근 가능
                .anyRequest().permitAll() // 그 외 요청은 모두 허용
            .and()
            .formLogin()  // 기본 로그인 폼 사용
                .loginPage("/login")  // 로그인 페이지 경로
                .permitAll()
            .and()
            .logout()  // 로그아웃 설정
                .permitAll();
        
        return http.build();
    }
}
