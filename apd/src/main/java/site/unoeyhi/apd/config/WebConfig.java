package site.unoeyhi.apd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // ✅ 모든 엔드포인트 허용
                .allowedOrigins("http://localhost:5173")  // ✅ 프론트엔드 도메인
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // ✅ 허용 메서드 추가
                .allowedHeaders("*")  // ✅ 모든 헤더 허용
                .allowCredentials(true);
    }
}
